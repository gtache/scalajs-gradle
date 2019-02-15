package com.github.gtache

import com.github.gtache.tasks.CompileJSTask
import com.google.common.collect.Sets
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Test
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.sem.Semantics
import scala.Option

import java.util.concurrent.locks.ReentrantLock

import static com.github.gtache.BuildConfig.*
import static com.github.gtache.Utils.*
import static com.github.gtache.tasks.CompileJSTask.*

class PluginTest extends GroovyTestCase {

    static final O_FILE = 'js2/js.js'
    static final OUTPUT_FILE = 'js2/js2.js'
    static final M_MODE = ECMA_6
    static final OUTPUT_MODE = ECMA_51_GLOBAL
    static final R_FILE = 'bla.js'
    static final REL_FILE = 'blabla.js'
    static final LOG_LEVEL = DEBUG

    @Test
    public void testAllConfigurations() {
        def optionsSet = [
                MIN_OUTPUT + "=" + O_FILE,
                OUTPUT + "=" + OUTPUT_FILE,
                MIN_PRETTY,
                PRETTY,
                MIN_N_SOURCEMAP,
                N_SOURCEMAP,
                //"compliantAsInstanceOfs",
                MIN_OUTPUTMODE + "=" + M_MODE,
                OUTPUTMODE + "=" + OUTPUT_MODE,
                MIN_CHECKIR,
                CHECKIR,
                MIN_RELSM + "=" + R_FILE,
                RELSM + "=" + REL_FILE,
                CompileJSTask.LOG_LEVEL + "=" + LOG_LEVEL,
                MIN_DEBUG,
                DEBUG,
                MIN_WARN,
                WARN,
                MIN_ERR,
                ERR
        ].toSet()
        def outputSet = [MIN_OUTPUT + "=" + O_FILE, OUTPUT + "=" + OUTPUT_FILE].toSet()
        def outputModeSet = [MIN_OUTPUTMODE + "=" + M_MODE, OUTPUTMODE + "=" + OUTPUT_MODE].toSet()
        def sourceMapSet = [MIN_RELSM + "=" + R_FILE, RELSM + "=" + REL_FILE].toSet()
        def logLevelSet = [CompileJSTask.LOG_LEVEL + "=" + LOG_LEVEL,
                           MIN_DEBUG, DEBUG,
                           MIN_WARN, WARN,
                           MIN_ERR, ERR].toSet()
        def setOptionsSet = new HashSet<Set<String>>()
        setOptionsSet.add(optionsSet)
        optionsSet.each {
            setOptionsSet.add([it].toSet())
        }
        def ret = (Sets.powerSet(outputSet) + Sets.powerSet(outputModeSet) + Sets.powerSet(sourceMapSet) + Sets.powerSet(logLevelSet) + setOptionsSet).toList()
        def numThreads = Runtime.getRuntime().availableProcessors()
        def threadList = new ArrayList<Thread>()
        def lock = new ReentrantLock()
        for (int i = 0; i < numThreads; ++i) {
            threadList.add(new Thread(new CheckRunnable(ret, i, numThreads, lock)))
        }
        for (int i = 0; i < numThreads; ++i) {
            threadList.get(i).start()
        }

        for (int i = 0; i < numThreads; ++i) {
            threadList.get(i).join()
        }

    }


    @Test
    public void testTasksPluginsDependenciesAdded() {
        def project = TestUtils.getFreshProject()
        TestUtils.applyPlugin(project)
        project.evaluate()
        def allTasks = [
                "TestJS",
                "FastOptJS",
                "FullOptJS",
                "RunJS",
                "NoOptJS"
        ]
        allTasks.each {
            assertTrue(project.tasks.findByPath(it) != null)
        }

        def plugins = [
                "java",
                "scala",
                "scalajs-plugin"
        ]

        plugins.each {
            assertTrue(project.plugins.findPlugin(it) != null)
        }

        def libDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-library_' + SCALA_VERSION, SCALAJS_VERSION)
        def compDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-compiler_' + SCALA_FULL_VERSION, SCALAJS_VERSION)
        def compileIt = project.configurations.getByName('compile').dependencies.iterator()
        def libDepFound = false
        while (compileIt.hasNext() && !libDepFound) {
            def dep = compileIt.next()
            if (libDep.group == dep.group && libDep.name == dep.name && libDep.version == dep.version) {
                libDepFound = true
            }
        }
        def scalaCompileIt = project.configurations.getByName('scalaCompilePlugin').dependencies.iterator()
        def compileDepFound = false
        while (scalaCompileIt.hasNext() && !compileDepFound) {
            def dep = scalaCompileIt.next()
            if (compDep.group == dep.group && compDep.name == dep.name && compDep.version == dep.version) {
                compileDepFound = true
            }
        }
        assertTrue(libDepFound && compileDepFound)
        deleteRecursive(project.projectDir)
    }

    private final class CheckRunnable implements Runnable {
        private final ArrayList<Set<String>> p
        private final int id
        private final int numThreads
        private final ReentrantLock lock
        private final int numOps
        private int counter = 0

        public CheckRunnable(List<Set<String>> p, int id, int numThreads, ReentrantLock lock) {
            this.p = p
            this.id = id
            this.numThreads = numThreads
            this.lock = lock
            this.numOps = (int) (p.size() / numThreads)
        }

        @Override
        public void run() {
            final int lowerBound = numOps * id
            final int upperBound = (id == (numThreads - 1)) ? p.size() : (numOps * (id + 1))
            for (int i = lowerBound; i < upperBound; ++i) {
                checkProperties(p.get(i))
                counter += 1
                println("ID : " + id + " finished : " + counter + "/" + numOps)
            }
        }

        private void checkProperties(Set<String> p) {
            lock.lock()
            final project = TestUtils.getFreshProject()
            lock.unlock()
            p.each {
                if (it.contains('=')) {
                    def res = it.split("=")
                    TestUtils.setProperty(project, res[0], res[1])
                } else {
                    TestUtils.setProperty(project, it)
                }
            }
            TestUtils.applyPlugin(project)
            project.evaluate() //Internal method, didn't find a way to do it (easily) via public API
            if (p.isEmpty()) {
                checkDefault(project)
            }
            p.each {
                checkProperty(it, p, project)
            }
            deleteRecursive(project.projectDir)
        }

        private void checkDefault(Project project) {
            final jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
            final jsBase = jsDir.absolutePath + File.separator + project.name
            final jsFile = project.file(jsBase + EXT)
            final jsFastFile = project.file(jsBase + FASTOPT_SUFFIX)
            final jsFullFile = project.file(jsBase + FULLOPT_SUFFIX)
            def options = ((CompileJSTask) project.tasks.findByName('FastOptJS')).options
            assertEquals(jsFastFile.path, options.output().path)
            assertEquals(Semantics.Defaults(), options.semantics())
            assertEquals(Level.Info$.MODULE$, options.logLevel())
            assertFalse(options.cp().isEmpty())
            assertTrue(options.sourceMap())
            assertFalse(options.checkIR())
            assertFalse(options.fullOpt())
            assertFalse(options.noOpt())
            assertFalse(options.prettyPrint())
            assertEquals(Option.apply(null), options.stdLib())
            assertEquals(Option.apply(null), options.relativizeSourceMap())
            options = ((CompileJSTask) project.tasks.findByName('FullOptJS')).options
            assertEquals(jsFullFile.path, options.output().path)
            options = ((CompileJSTask) project.tasks.findByName('NoOptJS')).options
            assertEquals(jsFile.path, options.output().path)
        }

        private void checkProperty(String s, Set<String> p, Project project) {
            final options = ((CompileJSTask) project.tasks.findByName('FastOptJS')).options
            switch (s) {
                case MIN_OUTPUT:
                    def projectP = project.file(project.property(s)).path
                    assertEquals(options.output().path, projectP)
                    assertEquals(project.file(O_FILE).path, projectP)
                    break
                case OUTPUT:
                    if (!p.contains(MIN_OUTPUT)) {
                        def projectP = project.file(project.property(s)).path
                        assertEquals(options.output().path, project.file(project.property(s)).path)
                        assertEquals(project.file(OUTPUT_FILE).path, projectP)
                    }
                    break
                case MIN_PRETTY:
                case PRETTY:
                    assertTrue(options.prettyPrint())
                    break
                case MIN_N_SOURCEMAP:
                case N_SOURCEMAP:
                    assertFalse(options.sourceMap())
                    break
                case COMPLIANT:
                    //TODO
                    break
                case MIN_OUTPUTMODE:
                    assertEquals(options.outputMode(), getOutputMode((String) project.property(s)))
                    assertEquals(M_MODE, (String) project.property(s))
                    break
                case OUTPUTMODE:
                    if (!p.contains(MIN_OUTPUTMODE)) {
                        assertEquals(options.outputMode(), getOutputMode((String) project.property(s)))
                        assertEquals(OUTPUT_MODE, (String) project.property(s))
                    }
                    break
                case MIN_CHECKIR:
                case CHECKIR:
                    assertTrue(options.checkIR())
                    break
                case MIN_RELSM:
                    def projectP = project.file(project.property(s)).path
                    assertEquals(options.relativizeSourceMap().get(), project.file((String) project.property(s)).toURI())
                    assertEquals(project.file(R_FILE).path, projectP)
                    break
                case RELSM:
                    if (!p.contains(MIN_RELSM)) {
                        def projectP = project.file(project.property(s)).path
                        assertEquals(options.relativizeSourceMap().get(), project.file((String) project.property(s)).toURI())
                        assertEquals(project.file(REL_FILE).path, projectP)
                    }
                    break
                case CompileJSTask.LOG_LEVEL:
                    if (!(p.contains(MIN_WARN) || p.contains(MIN_ERR) || p.contains(MIN_DEBUG) ||
                            p.contains(WARN) || p.contains(ERR) || p.contains(DEBUG))) {
                        assertEquals(
                                resolveLogLevel(project, (String) project.property(CompileJSTask.LOG_LEVEL), Level.Info$.MODULE$),
                                options.logLevel())
                        assertEquals(LOG_LEVEL, (String) project.property(s))
                    }
                    break
                case MIN_DEBUG:
                case DEBUG:
                    assertEquals(Level.Debug$.MODULE$, options.logLevel())
                    break
                case MIN_WARN:
                case WARN:
                    if (!(p.contains(MIN_DEBUG) || p.contains(DEBUG))) {
                        assertEquals(Level.Warn$.MODULE$, options.logLevel())
                    }
                    break
                case MIN_ERR:
                case ERR:
                    if (!(p.contains(MIN_DEBUG) || p.contains(DEBUG) ||
                            p.contains(MIN_WARN) || p.contains(WARN))) {
                        assertEquals(Level.Error$.MODULE$, options.logLevel())
                    }
                    break
                default:
                    break
            }
        }
    }

}
