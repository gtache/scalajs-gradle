package com.github.gtache

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
import org.scalajs.jsenv.rhino.RhinoJSEnv
import scala.collection.Map$
import scala.collection.Seq$
import scala.collection.immutable.List$
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.Seq

import java.nio.file.AccessDeniedException
import java.nio.file.Files

public final class Utils {

    public static final String SCALA_VERSION = "2.11"
    public static final String COMPILER_VERSION = "8"
    public static final String SCALAJS_VERSION = "0.6.9"

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

    //LogLevel
    public static final String ERROR_LVL = 'Error'
    public static final String WARN_LVL = 'Warn'
    public static final String INFO_LVL = 'Info'
    public static final String DEBUG_LVL = 'Debug'

    private Utils() {}

    /**
     * Resolves the level of logging, depending on the project properties
     * @param project The project with the property to check
     * @param property The property used on the switch
     * @return The level of logging (default : Debug)
     */
    public static Level resolveLogLevel(Project project, String property, Level base) {
        def level = base
        if (project.hasProperty(property)) {
            switch (project.property(property)) {
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
        if (project.hasProperty(RHINO)) {
            env = new RhinoJSEnv(Scalajsld$.MODULE$.options().semantics(), false)
        } else if (project.hasProperty(PHANTOM)) {
            env = new PhantomJSEnv("phantomjs", List$.MODULE$.empty(), Map$.MODULE$.empty(), true, null)
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
        final def hasTest = isTaskInStartParameter(project, 'testjs')

        final def o = 'o'
        final def output = 'output'
        if (project.hasProperty(o)) {
            path = project.file(project.property(o))
        } else if (project.hasProperty(output)) {
            path = project.file(project.property(output))
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

    public static void resolvePathAsync(Project project, UtilsListener listener, isUtils = false){
        project.gradle.taskGraph.whenReady { TaskExecutionGraph graph ->
            String path
            final def buildPath = project.buildDir.absolutePath
            final def jsPath = buildPath + JS_REL_DIR
            final def baseFilename = jsPath + project.name
            final def hasTest = graph.hasTask(':TestJS')

            final def o = 'o'
            final def output = 'output'
            if (project.hasProperty(o)) {
                path = project.file(project.property(o))
            } else if (project.hasProperty(output)) {
                path = project.file(project.property(output))
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
            if(isUtils){
                returnDependencySeq(project, path,listener)
            } else {
                listener.getResult(path)
            }
        }
    }

    public static void getMinimalDependencySeqAsync(Project project, UtilsListener listener){
        resolvePathAsync(project, listener, true)
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
    //FIXME Doesn't work if user doesn't explicitely writes the name of the task
    public static boolean isTaskInStartParameter(Project project, String task) {
        List<String> tasks = project.gradle.startParameter.taskNames.collect { it.toLowerCase() }
        return tasks.contains(task.toLowerCase())
    }

    private static void returnDependencySeq(Project project, String result, UtilsListener listener) {
        final FileVirtualJSFile file = new FileVirtualJSFile(project.file(result))
        final ResolvedJSDependency fileD = ResolvedJSDependency.minimal(file)
        final Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(1)
        dependencySeq.update(0, fileD)
        listener.getResult(dependencySeq)
    }
}
