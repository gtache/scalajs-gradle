package com.github.gtache.tasks

import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.jsdep.ResolutionInfo
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.jsenv.JSEnv
import scala.Option
import scala.collection.immutable.List$
import scala.collection.immutable.Set$
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
        final JSEnv env = Utils.resolveEnv(project)
        final String toExec = resolveToExec()
        final String path = resolvePath()
        final Level logLevel = Utils.resolveLogLevel(project, 'runLogLevel', Level.Debug$.MODULE$)

        final MemVirtualJSFile code = new MemVirtualJSFile("")
        final FileVirtualJSFile file = new FileVirtualJSFile(project.file(path))
        final ResolutionInfo fileI = new ResolutionInfo(
                file.path(),
                Set$.MODULE$.empty(),
                List$.MODULE$.empty(),
                Option.apply(null),
                Option.apply(null))
        final ResolvedJSDependency fileD = new ResolvedJSDependency(file, Option.apply(null), fileI)
        code.content_$eq(toExec)
        final Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(1)
        dependencySeq.update(0, fileD)

        env.jsRunner(dependencySeq, code).run(
                new ScalaConsoleLogger(logLevel),
                ConsoleJSConsole$.MODULE$)
    }

    /**
     * Resolves the code to execute, depending on the project properties
     * @return The code to execute
     */
    private String resolveToExec() {
        def toExec = null
        if (project.properties.containsKey('toExec')) {
            toExec = project.properties.get('toExec')
        } else if (project.properties.containsKey('classname')) {
            final def classname = project.properties.get('classname');
            if (!project.properties.containsKey('methname')) {
                toExec = classname + '().main()'
            } else {
                toExec = classname + '().' + project.properties.get('methname')
            }
        }
        return toExec
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
