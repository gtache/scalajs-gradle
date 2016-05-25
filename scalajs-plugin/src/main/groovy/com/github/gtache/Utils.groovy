package com.github.gtache

import com.github.gtache.testing.TestFramework
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileCollection
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
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

/**
 * Helper class for the plugin
 */
public final class Utils {

    public static final String SCALA_VERSION = "2.11"
    public static final String SUB_VERSION = "8"
    public static final String SCALAJS_VERSION = "0.6.9"

    public static final String JETTY_SERVER_VERSION = "8.1.16.v20140903"
    public static final String JETTY_WEBSOCKET_VERSION = "8.1.16.v20140903"

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

    //Envs
    public static final String RHINO = 'rhino'
    public static final String PHANTOM = 'phantom'
    public static final String JSENV = 'jsEnv'

    //LogLevel
    public static final String ERROR_LVL = 'error'
    public static final String WARN_LVL = 'warn'
    public static final String INFO_LVL = 'info'
    public static final String DEBUG_LVL = 'debug'


    public static final String TEST_FRAMEWORKS = 'testFrameworks'

    private static TaskExecutionGraph graph;

    private Utils() {}

    /**
     * Prepares the TaskExecutionGraph for the given project
     * @param project
     */
    public static void prepareGraph(Project project) {
        graph = null
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
        def env
        if (project.hasProperty(JSENV)){
            def envObj = project.property(JSENV)
            if (envObj instanceof JSEnv){
                env=envObj as JSEnv
            } else {
                project.logger.error("The object given as \"jsEnv\" is not of type JSEnv")
                env = null
            }
        }
        else if (project.hasProperty(RHINO)) {
            env = new RhinoJSEnv(Scalajsld$.MODULE$.options().semantics(), false)
        } else if (project.hasProperty(PHANTOM)) {
            final URL[] jars = project.configurations.phantomJetty.findAll {
                it.absolutePath.contains('jetty')
            }.collect { it.toURI().toURL() } as URL[]
            final PhantomJettyClassLoader loader = new PhantomJettyClassLoader(new URLClassLoader(jars), project.buildscript.classLoader)
            env = new PhantomJSEnv("phantomjs", List$.MODULE$.empty(), Map$.MODULE$.empty(), true, loader)
        } else {
            env = new NodeJSEnv("node", Seq$.MODULE$.empty(), Map$.MODULE$.empty())
        }
        return env
    }

    /**
     * Returns the outputMode corresponding to a string
     * @param s The string
     * @return the outputMode, or null
     */
    public static OutputMode getOutputMode(String s) {
        String toCompare = s.toLowerCase()
        if (toCompare == "ecmascript51global") {
            return OutputMode.ECMAScript51Global$.MODULE$
        } else if (toCompare == "ecmascript51isolated") {
            return OutputMode.ECMAScript51Isolated$.MODULE$
        } else if (toCompare == "ecmascript6") {
            return OutputMode.ECMAScript6$.MODULE$
        } else {
            return null
        }
    }

    /**
     * Resolves the path of the file to run, depending on full, fast or no optimization
     * @return The path of the file
     */
    public static String resolvePath(Project project) {
        def path
        final def buildPath = project.buildDir.absolutePath
        final def jsPath = buildPath + JS_REL_DIR
        final def baseFilename = jsPath + project.name
        if (graph == null) {
            project.logger.warn('TaskGraph not ready yet : Possible error when computing output file\n' +
                    'Run with TestJS explicitely to be sure it works')
        }
        //Performs suboptimal check if TaskExecutionGraph not ready
        final def hasTest = graph != null ? graph.hasTask(':TestJS') : isTaskInStartParameter(project, 'testjs')

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
        final Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(1)
        dependencySeq.update(0, fileD)
        dependencySeq
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
     * Checks if there is a task is to be executed (explicitely specified)
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
