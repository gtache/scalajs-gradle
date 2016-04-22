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

class PluginTest extends GroovyTestCase {

    static final def O_FILE = 'js2/js.js'
    static final def OUTPUT_FILE = 'js2/js2.js'
    static final def M_MODE = 'ECMAScript6'
    static final def OUTPUT_MODE = 'ECMAScript51Global'
    static final def R_FILE = 'bla.js'
    static final def REL_FILE = 'blabla.js'
    static final def LOG_LEVEL = 'Debug'

    @Test
    public void testAllConfigurations() {
        def optionsSet = [
                "o=" + O_FILE,
                "output=" + OUTPUT_FILE,
                "p",
                "prettyPrint",
                "s",
                "sourceMap",
                //"compliantAsInstanceOfs",
                "m=" + M_MODE,
                "outputMode=" + OUTPUT_MODE,
                "c",
                "checkIR",
                "r=" + R_FILE,
                "relativizeSourceMap=" + REL_FILE,
                "linkLogLevel=" + LOG_LEVEL,
                "d",
                "debug",
                "q",
                "quiet",
                "qq",
                "really-quiet"
        ].toSet()
        def outputSet = ["o=" + O_FILE, "output=" + OUTPUT_FILE].toSet()
        def outputModeSet = ["m=" + M_MODE, "outputMode=" + OUTPUT_MODE].toSet()
        def sourceMapSet = ["r=" + R_FILE, "relativizeSourceMap=" + REL_FILE].toSet()
        def logLevelSet = ["linkLogLevel=" + LOG_LEVEL, "d", "debug", "q", "quiet", "qq", "really-quiet"].toSet()
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
                "NoOptJS",
                "CleanAll"
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

        def libDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-library_2.11', '0.6.8')
        def compDep = new DefaultExternalModuleDependency('org.scala-js', 'scalajs-compiler_2.11.8', '0.6.8')
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
        assertTrue(libDepFound)
        assertTrue(compileDepFound)
        Utils.deleteRecursive(project.projectDir)
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
            this.numOps = p.size() / numThreads
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
            final def project = TestUtils.getFreshProject()
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
            Utils.deleteRecursive(project.projectDir)
        }

        private void checkDefault(Project project) {
            final def jsDir = project.file('js/')
            final def jsFile = project.file(jsDir.path + '/' + project.name + '.js')
            final def jsFastFile = project.file(jsDir.path + '/' + project.name + '_fastopt.js')
            final def jsFullFile = project.file(jsDir.path + '/' + project.name + '_fullopt.js')
            def options = ((CompileJSTask) project.tasks.findByName('FastOptJS')).options
            assertEquals(jsFastFile.path, options.output().path)
            assertEquals(Semantics.Defaults(), options.semantics())
            assertEquals(Level.Info$.MODULE$, options.logLevel())
            assertFalse(options.cp().isEmpty())
            assertFalse(options.jsoutput())
            assertFalse(options.sourceMap())
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
            final def options = ((CompileJSTask) project.tasks.findByName('FastOptJS')).options
            switch (s) {
                case 'o':
                    def projectP = project.file(project.property(s)).path
                    assertEquals(options.output().path, projectP)
                    assertEquals(project.file(O_FILE).path, projectP)
                    break
                case 'output':
                    if (!p.contains('o')) {
                        def projectP = project.file(project.property(s)).path
                        assertEquals(options.output().path, project.file(project.property(s)).path)
                        assertEquals(project.file(OUTPUT_FILE).path, projectP)
                    }
                    break
                case 'p':
                case 'prettyPrint':
                    assertTrue(options.prettyPrint())
                    break
                case 's':
                case 'sourceMap':
                    assertTrue(options.sourceMap())
                    break
                case 'compliantAsInstanceOfs':
                    //TODO
                    break
                case 'm':
                    assertEquals(options.outputMode(), Utils.getOutputMode((String) project.property(s)))
                    assertEquals(M_MODE, (String) project.property(s))
                    break
                case 'outputMode':
                    if (!p.contains('m')) {
                        assertEquals(options.outputMode(), Utils.getOutputMode((String) project.property(s)))
                        assertEquals(OUTPUT_MODE, (String) project.property(s))
                    }
                    break
                case 'c':
                case 'checkIR':
                    assertTrue(options.checkIR())
                    break
                case 'r':
                    def projectP = project.file(project.property(s)).path
                    assertEquals(options.relativizeSourceMap().get(), project.file((String) project.property(s)).toURI())
                    assertEquals(project.file(R_FILE).path, projectP)
                    break
                case 'relativizeSourceMap':
                    if (!p.contains('r')) {
                        def projectP = project.file(project.property(s)).path
                        assertEquals(options.relativizeSourceMap().get(), project.file((String) project.property(s)).toURI())
                        assertEquals(project.file(REL_FILE).path, projectP)
                    }
                    break
                case 'linkLogLevel':
                    if (!(p.contains('q') || p.contains('qq') || p.contains('d') ||
                            p.contains('quiet') || p.contains('really-quiet') || p.contains('debug'))) {
                        assertEquals(Utils.resolveLogLevel(project, (String) project.property('linkLogLevel'), Level.Info$.MODULE$),
                                options.logLevel())
                        assertEquals(LOG_LEVEL, (String) project.property(s))
                    }
                    break
                case 'd':
                case 'debug':
                    assertEquals(Level.Debug$.MODULE$, options.logLevel())
                    break
                case 'q':
                case 'quiet':
                    if (!(p.contains('d') || p.contains('debug'))) {
                        assertEquals(Level.Warn$.MODULE$, options.logLevel())
                    }
                    break
                case 'qq':
                case 'really-quiet':
                    if (!(p.contains('d') || p.contains('debug') ||
                            p.contains('q') || p.contains('quiet'))) {
                        assertEquals(Level.Error$.MODULE$, options.logLevel())
                    }
                    break
                default:
                    break
            }
        }
    }

}
