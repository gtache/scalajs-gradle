package com.github.gtache.tasks

import com.github.gtache.Utils
import com.github.gtache.testing.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ComJSEnv
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.ScalaJSFramework
import sbt.testing.*
import scala.collection.JavaConverters
import scala.collection.mutable.Seq

public class TestJSTask extends DefaultTask {
    final String description = "Runs tests"

    private static final String LOG_LEVEL = 'testLogLevel'

    /**
     * The action of the task : Instantiates a framework, a runner, and executes all tests found, with the fingerprints
     * given by the framework
     */
    //TODO not functioning
    @TaskAction
    def run() {
        final Seq<ResolvedJSDependency> dependencySeq = Utils.getMinimalDependencySeq(project)
        final def libEnv = (ComJSEnv) Utils.resolveEnv(project).loadLibs(dependencySeq)

        final List<ScalaJSFramework> frameworks = JavaConverters.asJavaIterableConverter(new FrameworkDetector(libEnv).instantiatedScalaJSFrameworks(
                TestFrameworks.allFrameworks(),
                libEnv,
                new ScalaConsoleLogger(Utils.resolveLogLevel(project, LOG_LEVEL, Level.Debug$.MODULE$)),
                ConsoleJSConsole$.MODULE$
        )).asJava().toList()

        /*
        final Framework framework = new ScalaJSFramework(
                'org.scalatest.tools.ScalaTestFramework',
                libEnv,
                new ScalaConsoleLogger(Utils.resolveLogLevel(project,LOG_LEVEL, Level.Debug$.MODULE$)),
                ConsoleJSConsole$.MODULE$)
        */
        final URL[] urls = project.sourceSets.test.runtimeClasspath.collect { it.toURI().toURL() }.toArray(new URL[0])
        final URLClassLoader classL = new URLClassLoader(urls)

        frameworks.each { ScalaJSFramework framework ->
            final Runner runner = framework.runner(new String[0], new String[0], null)
            final Fingerprint[] fingerprints = framework.fingerprints()
            final Task[] tasks = runner.tasks(ClassScanner.scan(classL, fingerprints))
            final ScalaJSTestStatus testStatus = new ScalaJSTestStatus(framework)
            final EventHandler eventHandler = new ScalaJSEventHandler(testStatus)
            testStatus.runner_$eq(runner)
            println("Framework : " + framework.name())
            println("Tasks : ")
            tasks.each { println(it.taskDef().fullyQualifiedName()) }
            for (Task t : tasks) {
                testStatus.all_$eq(testStatus.all().$colon$colon(t))
                t.execute(eventHandler, [new LoggerWrapper(framework.logger())] as Logger[])
            }
        }
    }
}
