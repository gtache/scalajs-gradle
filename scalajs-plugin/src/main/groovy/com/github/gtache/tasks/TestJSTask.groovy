package com.github.gtache.tasks

import com.github.gtache.Utils
import com.github.gtache.testing.ClassScanner
import com.github.gtache.testing.ScalaJSEventHandler$
import com.github.gtache.testing.ScalaJSTestStatus$
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ComJSEnv
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.ScalaJSFramework
import sbt.testing.*
import scala.collection.mutable.Seq

public class TestJSTask extends DefaultTask {
    final String description = "Runs tests"

    /**
     * The action of the task : Instantiates a framework, a runner, and executes all tests found, with the fingerprints
     * given by the framework
     */
    //TODO not functioning
    @TaskAction
    def run() {
        final Seq dependencySeq = Utils.getMinimalDependencySeq(project)
        final def libEnv = (ComJSEnv) Utils.resolveEnv(project).loadLibs(dependencySeq)

        logger.debug("dependencySeq size : " + dependencySeq.size())
        final def name = dependencySeq.apply(0).lib().name()
        final def content = dependencySeq.apply(0).lib().content()
        logger.debug("lib name : " + name)
        logger.debug("lib content : " + content)

        final Framework framework = new ScalaJSFramework(
                "ScalaJS Testing framework",
                libEnv,
                new ScalaConsoleLogger(Utils.resolveLogLevel(project, 'testLogLevel', Level.Debug$.MODULE$)),
                ConsoleJSConsole$.MODULE$)
        final Runner runner = framework.runner(new String[0], new String[0], null)
        final URLClassLoader classL = new URLClassLoader(project.buildDir.toURI().toURL())
        final Task[] tasks = runner.tasks(ClassScanner.scan(classL, framework.fingerprints()))
        final EventHandler eventHandler = ScalaJSEventHandler$.MODULE$
        final ScalaJSTestStatus$ memory = ScalaJSTestStatus$.MODULE$
        memory.runner_$eq(runner)
        println("Framework : " + framework)
        println("Runner : " + runner)
        println("URLClassLoader : " + classL)
        println("Tasks : " + tasks)
        for (Task t : tasks) {
            t.execute(eventHandler, [framework.logger()] as Logger[])
            memory.all_$eq(memory.all() + t)
        }
    }


}
