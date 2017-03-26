package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.jsenv.JSEnv

/**
 * Task used to run a js file
 */
public class RunJSTask extends DefaultTask {
    final String description = "Runs the generated js file.\n" +
            "Needs Node.js / PhantomJS on PATH, or use Rhino.\n" +
            "Use -Prhino (highest priority) or -Pphantom. Default : node"

    private static final String LOG_LEVEL = 'runLogLevel'
    private static final String EXEC_FILE = 'fileToExec'
    private static final String EXEC_CODE = 'toExec'
    private static final String CLASSNAME = 'classname'
    private static final String METHNAME = 'methname'

    /**
     * The main method of the task, resolves the environment and the code to execute, and runs it
     */
    @TaskAction
    def run() {
        final Tuple2<Boolean, String> toExec = resolveToExec()
        if (toExec.second == null) {
            logger.error('Nothing to execute')
        } else {
            final JSEnv env = Utils.resolveEnv(project)
            final Level logLevel = Utils.resolveLogLevel(project, LOG_LEVEL, Level.Debug$.MODULE$)
            final dependencySeq = Utils.getMinimalDependencySeq(project)
            VirtualJSFile code
            if (toExec.first) {
                code = new FileVirtualJSFile(project.file(toExec.second))
            } else {
                code = new MemVirtualJSFile("userInputCode.js")
                code.content_$eq(toExec.second)
            }


            logger.info('Running env ' + env.name() + ' with code ' + code.name() + ' and dependency ' + dependencySeq)
            env.jsRunner(dependencySeq, code).run(
                    new ScalaConsoleLogger(logLevel),
                    ConsoleJSConsole$.MODULE$)
        }
    }

    /**
     * Resolves the code to execute, depending on the project properties
     * @return The code to execute
     */
    private Tuple2<Boolean, String> resolveToExec() {
        def toExec = null
        def isFile = false
        if (project.hasProperty(EXEC_FILE)) {
            toExec = project.property(EXEC_FILE)
            isFile = true
        } else if (project.hasProperty(EXEC_CODE)) {
            toExec = project.property(EXEC_CODE)
        } else if (project.hasProperty(CLASSNAME)) {
            final classname = project.property(CLASSNAME)
            if (!project.hasProperty(METHNAME)) {
                toExec = classname + '().main()'
            } else {
                toExec = classname + '().' + project.property(METHNAME)
            }
        }
        return new Tuple(isFile, toExec)
    }
}
