package com.github.gtache.tasks

import com.github.gtache.Utils
import com.github.gtache.UtilsListener
import com.github.gtache.testing.ClassScanner
import com.github.gtache.testing.ScalaJSEventHandler$
import com.github.gtache.testing.ScalaJSTestStatus$
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ComJSEnv
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.ScalaJSFramework
import sbt.testing.*
import scala.collection.mutable.Seq

public class TestJSTask extends DefaultTask implements UtilsListener<Seq<ResolvedJSDependency>>{
    final String description = "Runs tests"

    private static final String LOG_LEVEL = 'testLogLevel'

    private Seq<ResolvedJSDependency> dependencySeq = null

    /**
     * The action of the task : Instantiates a framework, a runner, and executes all tests found, with the fingerprints
     * given by the framework
     */
    //TODO not functioning
    @TaskAction
    def run() {
        final def libEnv = (ComJSEnv) Utils.resolveEnv(project).loadLibs(dependencySeq)

        final Framework framework = new ScalaJSFramework(
                "ScalaJS Testing framework",
                libEnv,
                new ScalaConsoleLogger(Utils.resolveLogLevel(project, LOG_LEVEL, Level.Debug$.MODULE$)),
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

    def callUtils() {
        Utils.getMinimalDependencySeqAsync(project,this)
    }

    @Override
    public void getResult(Seq<ResolvedJSDependency> result) {
        this.dependencySeq=result
    }
}
