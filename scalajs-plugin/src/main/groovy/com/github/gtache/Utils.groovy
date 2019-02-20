package com.github.gtache

import com.github.gtache.testing.TestFramework
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileCollection
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.jsdep.ResolvedJSDependency$
import org.scalajs.core.tools.linker.backend.ModuleKind
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv$
import org.scalajs.jsenv.phantomjs.PhantomJettyClassLoader
import org.scalajs.jsenv.rhino.RhinoJSEnv
import scala.collection.Map$
import scala.collection.Seq$
import scala.collection.immutable.List$
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.Seq

import java.nio.file.AccessDeniedException
import java.nio.file.Files

import static com.github.gtache.tasks.CompileJSTask.MIN_OUTPUT
import static com.github.gtache.tasks.CompileJSTask.OUTPUT
import static org.scalajs.core.ir.Utils.escapeJS

/**
 * Helper class for the plugin
 */
public final class Utils {

    public static final String JETTY_SERVER_VERSION = "8.1.16.v20140903"
    public static final String JETTY_WEBSOCKET_VERSION = "8.1.16.v20140903"

    public static final String CPSeparator = File.pathSeparator
    //All parameters for generated js file
    public static final String JS_REL_DIR = File.separator + 'js' + File.separator
    public static final String EXT = '.js'
    public static final String FULLOPT_SUFFIX = '_fullopt' + EXT
    public static final String FASTOPT_SUFFIX = '_fastopt' + EXT
    public static final String TEST_SUFFIX = '_test'
    public static final String NOOPT_TEST_SUFFIX = TEST_SUFFIX + EXT
    public static final String FULLOPT_TEST_SUFFIX = TEST_SUFFIX + FULLOPT_SUFFIX
    public static final String FASTOPT_TEST_SUFFIX = TEST_SUFFIX + FASTOPT_SUFFIX

    public static final String RUN_FULL = 'runFull'
    public static final String RUN_NOOPT = 'runNoOpt'

    public static final String JAVA_OPT = 'javaOpt'

    //Envs
    public static final String RHINO = 'rhino'
    public static final String PHANTOM = 'phantom'
    public static final String JSENV = 'jsEnv'
    public static final String JSDOM = 'jsDom'

    //LogLevel
    public static final String ERROR_LVL = 'error'
    public static final String WARN_LVL = 'warn'
    public static final String INFO_LVL = 'info'
    public static final String DEBUG_LVL = 'debug'

    //Module
    public static final String COMMONJS_MODULE = "commonJSModule"
    public static final String ES_MODULE = "esModule"
    public static final String USE_MAIN_MODULE = "useMainModuleInit"

    public static final String TEST_FRAMEWORKS = 'testFrameworks'

    private static TaskExecutionGraph graph

    private Utils() {}

    /**
     * Prepares the TaskExecutionGraph for the given project
     * @param project
     */
    public static void prepareGraph(Project project) {
        graph = null //Need to reset, as if we are using a daemon it will be saved
        project.gradle.taskGraph.whenReady { graph = it }
    }

    /**
     * Resolves the level of logging, depending on the project properties
     * @param project The project with the property to check
     * @param property The property used on the switch
     * @return The level of logging (default : Debug)
     */
    public static Level resolveLogLevel(Project project, String property, Level base) {
        def level = base
        if (project.hasProperty(property)) {
            switch ((project.property(property) as String).toLowerCase()) {
                case ERROR_LVL:
                    level = Level.Error$.MODULE$
                    break
                case WARN_LVL:
                    level = Level.Warn$.MODULE$
                    break
                case INFO_LVL:
                    level = Level.Info$.MODULE$
                    break
                case DEBUG_LVL:
                    level = Level.Debug$.MODULE$
                    break
                default:
                    project.logger.warn("Unknown log level : " + project.property(property))
                    break
            }
        }
        return level
    }

    /**
     * Resolves the environment to use, depending on the project properties
     * @param project The project with properties to check
     * @return The environment to use (Default : Node)
     */
    public static JSEnv resolveEnv(Project project) {
        JSEnv env
        if (project.hasProperty(JSENV)) {
            def envObj = project.property(JSENV)
            if (envObj instanceof JSEnv) {
                env = envObj
            } else {
                project.logger.error("JSEnv not of type JSEnv ; was " + envObj.getClass())
                env = null
            }
        } else if (project.hasProperty(RHINO)) {
            //TODO will be deprecated
            env = new RhinoJSEnv(Scalajsld$.MODULE$.options().semantics(), false)
        } else if (project.hasProperty(PHANTOM)) {
            final URL[] jars = project.configurations.phantomJetty.findAll {
                it.absolutePath.contains('jetty')
            }.collect { it.toURI().toURL() } as URL[]
            final PhantomJettyClassLoader loader = new PhantomJettyClassLoader(new URLClassLoader(jars), project.buildscript.classLoader)
            def config = PhantomJSEnv.Config$.MODULE$.apply()
                    .withExecutable("phantomjs")
                    .withArgs(List$.MODULE$.empty())
                    .withEnv(Map$.MODULE$.empty())
                    .withAutoExit(true)
                    .withJettyClassLoader(loader)
            env = new PhantomJSEnv(config)
        } else if (project.hasProperty(JSDOM)) {
            def config = new JSDOMNodeJSEnv.Config$().apply()
                    .withExecutable("node")
                    .withArgs(List$.MODULE$.empty())
                    .withEnv(Map$.MODULE$.empty())
            env = new JSDOMNodeJSEnv(config)
        } else {
            def config = NodeJSEnv.Config$.MODULE$.apply()
                    .withExecutable("node")
                    .withArgs(List$.MODULE$.empty())
                    .withEnv(Map$.MODULE$.empty())
                    .withSourceMap(true)
            env = new NodeJSEnv(config)
        }
        return env
    }

    /**
     * Resolves the path of the file to run, depending on full, fast or no optimization
     * @return The path of the file
     */
    public static String resolvePath(Project project) {
        final def buildPath = project.buildDir.absolutePath
        final def jsPath = buildPath + JS_REL_DIR
        final def baseFilename = jsPath + project.name
        if (graph == null) {
            project.logger.warn('TaskGraph not ready yet : Possible error when computing output file\n' +
                    'Run with TestJS explicitely to be sure it works')
        }
        //Performs suboptimal check if TaskExecutionGraph not ready
        final def hasTest = graph != null ? graph.hasTask(':TestJS') : isTaskInStartParameter(project, 'testjs')
        def path
        if (project.hasProperty(MIN_OUTPUT)) {
            path = project.file(project.property(MIN_OUTPUT))
        } else if (project.hasProperty(OUTPUT)) {
            path = project.file(project.property(OUTPUT))
        } else if (project.hasProperty(RUN_FULL)) {
            if (hasTest) {
                path = project.file(baseFilename + FULLOPT_TEST_SUFFIX).absolutePath
            } else {
                path = project.file(baseFilename + FULLOPT_SUFFIX).absolutePath
            }
        } else if (project.hasProperty(RUN_NOOPT)) {
            if (hasTest) {
                path = project.file(baseFilename + NOOPT_TEST_SUFFIX).absolutePath
            } else {
                path = project.file(baseFilename + EXT).absolutePath
            }
        } else {
            if (hasTest) {
                path = project.file(baseFilename + FASTOPT_TEST_SUFFIX).absolutePath
            } else {
                path = project.file(baseFilename + FASTOPT_SUFFIX).absolutePath
            }
        }
        return path
    }

    /**
     * Returns the minimal ResolvedDependency Seq, which is comprised of only the generated js file.
     * @param project The project
     * @return the (mutable) seq of only one element
     */
    public static Seq getMinimalDependencySeq(Project project) {
        final FileVirtualJSFile file = new FileVirtualJSFile(project.file(resolvePath(project)))
        final ResolvedJSDependency fileD = ResolvedJSDependency.minimal(file)
        final ResolvedJSDependency configurations = resolveConfigurationLibs(project)
        final Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(configurations == null ? 1 : 2)
        dependencySeq.update(0, fileD)
        if (configurations != null) {
            dependencySeq.update(1, configurations)
        }
        return dependencySeq
    }

    /**
     * Deletes a file, if it is a folder, deletes it recursively.
     * @param file The file to delete
     */
    public static void deleteRecursive(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                file.listFiles().each { deleteRecursive(it) }
            }
            try {
                if (Files.isWritable(file.toPath())) {
                    if (!(file.isDirectory() && file.listFiles().size() > 0)) {
                        Files.deleteIfExists(file.toPath())
                    }
                }
            }
            catch (AccessDeniedException e) {
                //Files.isWritable doesnt work 100% apparently => can't use project.delete, throws exception sometimes
            }
        }
    }

    /**
     * Prints file if it is a file, or all the files contained in file if it is a directory
     * @param file The file to print
     * @param project For logging (printing)
     */
    public static void listRecursive(File file, Project project) {
        if (file.isDirectory()) {
            file.listFiles().each {
                listRecursive(it, project)
            }
        } else {
            project.logger.info(file.name)
        }
    }

    /**
     * Prints all the files in a FileCollection (as well as subfiles if there are directories)
     * @param file The FileCollection
     * @param project For logging (printing)
     */
    public static void listRecursive(FileCollection file, Project project) {
        file.files.each {
            listRecursive(it, project)
        }
    }

    /**
     * Checks if there is a task to be executed (explicitely specified)
     * @param project The project whose startparameters we want to use
     * @param task The name task to be checked
     */
    public static boolean isTaskInStartParameter(Project project, String task) {
        List<String> tasks = project.gradle.startParameter.taskNames.collect { it.toLowerCase() }
        return tasks.contains(task.toLowerCase())
    }

    /**
     * Returns the list of custom TestFrameworks added by the user
     * @param project The project
     * @return A list of TestFramework
     */
    public static List<TestFramework> resolveTestFrameworks(Project project) {
        if (project.hasProperty(TEST_FRAMEWORKS)) {
            final List<String> frameworksName = (List<String>) project.property(TEST_FRAMEWORKS)
            return frameworksName.collect {
                final ArrayBuffer<String> seq = new ArrayBuffer<>()
                seq.$plus$eq(it)
                new TestFramework(seq.toSeq())
            }
        } else {
            return new ArrayList<>()
        }
    }

    public static ModuleKind resolveModuleKind(Project project) {
        return project.hasProperty(COMMONJS_MODULE) ? ModuleKind.CommonJSModule$.MODULE$ : ModuleKind.NoModule$.MODULE$
    }

    private static Map<String, String> resolveJavaSystemProperties(Project project) {
        if (project.hasProperty(JAVA_OPT)) {
            final HashMap<String, String> options = new HashMap<>()
            final javaOpt = project.property(JAVA_OPT) as String
            final beginning = javaOpt.substring(0, 2)
            if (beginning == '-D') {
                final pairs = javaOpt.substring(2, javaOpt.length()).split(CPSeparator)
                for (String pair : pairs) {
                    final keyV = pair.split('=')
                    if (keyV.length != 2) {
                        project.logger.error("javaOpt can only be \"-D<key1>=<value2>" + CPSeparator + "<key2>=<value2>...\", but received " + project.property(JAVA_OPT))
                        throw new IllegalArgumentException()
                    } else {
                        options.put(keyV[0], keyV[1])
                    }
                }
                options.each { project.logger.warn(it.getKey() + '->' + it.getValue()) }
                return options
            } else {
                project.logger.error("javaOpt can only be \"-D<key1>=<value2>" + CPSeparator + "<key2>=<value2>...\", but received " + project.property(JAVA_OPT))
                throw new IllegalArgumentException()
            }
        }
        return new HashMap<String, String>()
    }

    private static ResolvedJSDependency resolveConfigurationLibs(Project project) {
        final javaSystemProperties = resolveJavaSystemProperties(project)
        if (javaSystemProperties.isEmpty()) {
            return null
        } else {
            final formattedProps = javaSystemProperties.collect {
                "\"" + escapeJS(it.getKey()) + "\": \"" + escapeJS(it.getValue()) + "\""
            }
            formattedProps.each { project.logger.warn(it) }
            final String code =
                    "var __ScalaJSEnv = (typeof __ScalaJSEnv === \"object\" && __ScalaJSEnv) ? __ScalaJSEnv : {};\n" +
                            "__ScalaJSEnv.javaSystemProperties = {" + formattedProps.join(", ") + "};\n"

            return ResolvedJSDependency$.MODULE$.minimal(new MemVirtualJSFile("setJavaSystemProperties.js").withContent(code))
        }
    }

    /**
     * Takes a wildcard and returns a regex corresponding to it
     * Example : com.github.* => com\\.github\\..*
     * @param s The string to transform
     * @return The regex
     */
    public static String toRegex(String s) {
        s.collectReplacements { Character c ->
            switch (c) {
                case '*':
                    '.*'
                    break
                case '?':
                    '.'
                    break
                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '^':
                case '$':
                case '|':
                case '\\':
                    '\\' + c
                    break
                default:
                    null
                    break
            }
        }
    }
}
