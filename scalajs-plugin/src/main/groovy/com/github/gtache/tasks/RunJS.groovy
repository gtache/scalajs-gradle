package com.github.gtache.tasks

import com.github.gtache.Scalajsld$
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.io.FileVirtualJSFile
import org.scalajs.core.tools.io.MemVirtualJSFile
import org.scalajs.core.tools.jsdep.ResolutionInfo
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
import org.scalajs.jsenv.rhino.RhinoJSEnv
import scala.Option
import scala.collection.Map$
import scala.collection.Seq$
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
    private JSEnv env
    private String toExec

    @TaskAction
    public run() {
        setEnv()
        String path
        if (project.hasProperty('runFull')) {
            path = project.file('js/' + project.name + '_fullopt.js').absolutePath
        } else if (project.hasProperty('runNoOpt')) {
            path = project.file('js/' + project.name + '.js').absolutePath
        } else {
            path = project.file('js/' + project.name + '_fastopt.js').absolutePath
        }
        MemVirtualJSFile code = new MemVirtualJSFile("")
        FileVirtualJSFile file = new FileVirtualJSFile(project.file(path))
        ResolutionInfo fileI = new ResolutionInfo(
                file.path(),
                Set$.MODULE$.empty(),
                List$.MODULE$.empty(),
                Option.apply(null),
                Option.apply(null))
        ResolvedJSDependency fileD = new ResolvedJSDependency(file, Option.apply(null), fileI)
        setToExec()
        code.content_$eq(toExec)
        Seq<ResolvedJSDependency> dependencySeq = new ArraySeq<>(1)
        dependencySeq.update(0, fileD)
        env.jsRunner(dependencySeq, code).run(
                new ScalaConsoleLogger(Scalajsld$.MODULE$.options().logLevel()),
                ConsoleJSConsole$.MODULE$)
    }

    private setToExec() {
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
    }

    private setEnv() {
        if (project.hasProperty('rhino')) {
            env = new RhinoJSEnv(Scalajsld$.MODULE$.options().semantics(), false)
        } else if (project.hasProperty('phantom')) {
            env = new PhantomJSEnv("phantomjs", List$.MODULE$.empty(), Map$.MODULE$.empty(), true, null)
        } else {
            env = new NodeJSEnv("node", Seq$.MODULE$.empty(), Map$.MODULE$.empty())
        }
    }

}
