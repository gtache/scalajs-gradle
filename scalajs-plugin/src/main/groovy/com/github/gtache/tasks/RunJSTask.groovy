package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.io.VirtualJSFile
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.jsenv.JSEnv
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.Seq

/**
 * Task used to run a js file
 */
public class RunJSTask extends DefaultTask {
    final String description = "Runs the generated js file.\n" +
            "Needs Node.js / PhantomJS on PATH, or use Rhino.\n" +
            "Use -Prhino (highest priority) or -Pphantom. Default : node"

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
            final String path = resolvePath()
            final Level logLevel = Utils.resolveLogLevel(project, 'runLogLevel', Level.Debug$.MODULE$)

            VirtualJSFile code
            if (toExec.first) {
                code = new FileVirtualJSFile(project.file(toExec.second))
            } else {
                code = new MemVirtualJSFile("userInputCode.js")
                code.content_$eq(toExec.second)
            }

            final FileVirtualJSFile file = new FileVirtualJSFile(project.file(path))
            final ResolvedJSDependency fileD = ResolvedJSDependency.minimal(file)
            final Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(1)
            dependencySeq.update(0, fileD)

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
        if (project.hasProperty('fileToExec')) {
            toExec = project.property('fileToExec')
            isFile = true
        } else if (project.hasProperty('toExec')) {
            toExec = project.property('toExec')
        } else if (project.hasProperty('classname')) {
            final def classname = project.property('classname');
            if (!project.hasProperty('methname')) {
                toExec = classname + '().main()'
            } else {
                toExec = classname + '().' + project.property('methname')
            }
        }
        return new Tuple(isFile, toExec)
    }

    /**
     * Resolves the path of the file to run, depending on full, fast or no optimization
     * @return The path of the file
     */
    private String resolvePath() {
        def path
        if (project.hasProperty('o')) {
            path = project.file(project.property('o'))
        } else if (project.hasProperty('output')) {
            path = project.file(project.property('output'))
        } else if (project.hasProperty('runFull')) {
            path = project.file('js/' + project.name + '_fullopt.js').absolutePath
        } else if (project.hasProperty('runNoOpt')) {
            path = project.file('js/' + project.name + '.js').absolutePath
        } else {
            path = project.file('js/' + project.name + '_fastopt.js').absolutePath
        }
        return path
    }

}
