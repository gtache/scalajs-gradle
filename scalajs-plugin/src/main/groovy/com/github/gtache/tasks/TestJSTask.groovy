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
import scala.collection.Seq
import scala.collection.mutable.ArrayBuffer

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

        final List<TestFramework> customTestFrameworks = Utils.resolveTestFrameworks(project)
        final ArrayBuffer<TestFramework> allFrameworks = new ArrayBuffer<>()
        customTestFrameworks.each {
            allFrameworks.$plus$eq(it)
        }
        final Seq<TestFramework> defaultFrameworks = TestFrameworks.defaultFrameworks()
        //TODO improve
        for (int i = 0; i < defaultFrameworks.length(); ++i) {
            allFrameworks.$plus$eq(defaultFrameworks.apply(i))
        }
        final List<ScalaJSFramework> frameworks = JavaConverters.asJavaIterableConverter(new FrameworkDetector(libEnv).instantiatedScalaJSFrameworks(
                allFrameworks.toSeq(),
                libEnv,
                new ScalaConsoleLogger(Utils.resolveLogLevel(project, LOG_LEVEL, Level.Debug$.MODULE$)),
                ConsoleJSConsole$.MODULE$
        )).asJava().toList()

        final URL[] urls = project.sourceSets.test.runtimeClasspath.collect { it.toURI().toURL() } as URL[]
        final URLClassLoader classL = new URLClassLoader(urls)

        frameworks.each { ScalaJSFramework framework ->
            println("Framework found : " + framework.name())
        }

        Set<String> explicitelySpecified = new HashSet<>();
        scala.collection.immutable.Set<String> excluded = new scala.collection.immutable.HashSet<String>();
        if (project.hasProperty('test-only')) {
            explicitelySpecified = ((String) project.property('test-only')).split(File.pathSeparator).toList().toSet()
        } else if (project.hasProperty('test-quick')) {
            explicitelySpecified = ((String) project.property('test-quick')).split(File.pathSeparator).toList().toSet()
            excluded = ScalaJSTestResult$.MODULE$.successfulClassnames()
        } else if (project.hasProperty('retest')){
            excluded = ScalaJSTestResult$.MODULE$.successfulClassnames()
        }
        scala.collection.immutable.Set<String> explicitelySpecifiedScala = JavaConverters.asScalaSetConverter(explicitelySpecified).asScala().toSet()
        ScalaJSTestResult$.MODULE$.clear()

        Logger[] simpleLoggerArray = new SimpleLogger() as Logger[]
        frameworks.each { ScalaJSFramework framework ->
            final Runner runner = framework.runner(new String[0], new String[0], null)
            final Fingerprint[] fingerprints = framework.fingerprints()
            final Task[] tasks = runner.tasks(ClassScanner.scan(classL, fingerprints, explicitelySpecifiedScala, excluded))
            final ScalaJSTestStatus testStatus = new ScalaJSTestStatus(framework)
            final EventHandler eventHandler = new ScalaJSEventHandler(testStatus)
            testStatus.runner_$eq(runner)
            println("Executing " + framework.name())
            tasks.each { println(it.taskDef().fullyQualifiedName()) }
            tasks.each { Task t ->
                testStatus.all_$eq(testStatus.all().$colon$colon(t))
                t.execute(eventHandler, simpleLoggerArray)
            }
        }

        if (ScalaJSTestResult$.MODULE$.isSuccess()) {
            project.logger.lifecycle(ScalaJSTestResult$.MODULE$.toString() + "\nAll tests passed")
        } else {
            project.logger.lifecycle(ScalaJSTestResult$.MODULE$.toString() + "\nSome tests failed")
        }
    }
}
