package com.github.gtache.tasks

import com.github.gtache.Utils
import com.github.gtache.testing.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.logging.ScalaConsoleLogger
import org.scalajs.jsenv.ComJSEnv
import org.scalajs.jsenv.ConsoleJSConsole$
import org.scalajs.testadapter.TestAdapter
import sbt.testing.*
import scala.Option
import scala.collection.JavaConverters
import scala.collection.Seq
import scala.collection.mutable.ArrayBuffer

import static com.github.gtache.Utils.CPSeparator

/**
 * A task used to run tests for various frameworks
 */
class TestJSTask extends DefaultTask {
    final String description = "Runs tests. Depends on a CompileJS task.\n" +
            "Can use -PtestLogLevel to change the level of logging, \n" +
            "-Ptest-only='...' to test only the given files, \n" +
            "-Ptest-quick='...' to test only the given files which have failed during the last testing, \n" +
            "-Pretest to run only tests which have failed during the last testing."

    private static final String LOG_LEVEL = 'testLogLevel'
    private static final String TEST_ONLY = 'test-only'
    private static final String TEST_QUICK = 'test-quick'
    private static final String RETEST = 'retest'

    /**
     * The action of the task : Instantiates a framework, a runner, and executes all tests found, with the fingerprints
     * given by the framework
     */
    @TaskAction
    def run() {
        final Seq<ResolvedJSDependency> dependencySeq = Utils.getMinimalDependencySeq(project)
        final libEnv = (ComJSEnv) Utils.resolveEnv(project).loadLibs(dependencySeq)

        final List<TestFramework> customTestFrameworks = Utils.resolveTestFrameworks(project)
        final ArrayBuffer<TestFramework> allFrameworks = new ArrayBuffer<>()
        customTestFrameworks.each {
            allFrameworks.$plus$eq(it)
        }
        final Seq<TestFramework> defaultFrameworks = TestFrameworks.defaultFrameworks()
        for (int i = 0; i < defaultFrameworks.length(); ++i) {
            allFrameworks.$plus$eq(defaultFrameworks.apply(i))
        }
        def config = TestAdapter.Config$.MODULE$.apply()
                .withJSConsole(ConsoleJSConsole$.MODULE$)
                .withLogger(new ScalaConsoleLogger(Utils.resolveLogLevel(project, LOG_LEVEL, Level.Info$.MODULE$)))
                .withModuleSettings(Utils.resolveModuleKind(project), Option.empty())
        TestAdapter adapter = new TestAdapter(libEnv, config)
        def jFrameworks = JavaConverters.bufferAsJavaList(allFrameworks)
        jFrameworks = jFrameworks.collect {
            it.toList()
        }
        def frameworksO = adapter.loadFrameworks(JavaConverters.asScalaBuffer(jFrameworks).toList())
        JavaConverters.asJavaCollection(frameworksO).each {
            if (it.nonEmpty()) {
                project.logger.info("Framework found : " + it.get().name())
            }
        }

        final URL[] urls = project.sourceSets.test.runtimeClasspath.collect { it.toURI().toURL() } as URL[]
        final URLClassLoader classL = new URLClassLoader(urls)

        final String objWildcard = '\\$?'
        Set<String> explicitlySpecified = new HashSet<>()
        Set<String> excluded = new HashSet<String>()
        if (project.hasProperty(TEST_ONLY)) {
            explicitlySpecified = ((String) project.property(TEST_ONLY)).split(CPSeparator).toList().toSet()
                    .collect { Utils.toRegex(it) }
            project.logger.info("Regex to add " + explicitlySpecified)
            if (explicitlySpecified.isEmpty()) {
                explicitlySpecified.add("")
            }
        } else if (project.hasProperty(TEST_QUICK)) {
            //FIXME if last run didn't contain explicitlySpecified, will run them anyway
            explicitlySpecified = ((String) project.property(TEST_QUICK)).split(CPSeparator).toList().toSet()
                    .collect { Utils.toRegex(it) }
            project.logger.info("Regex to add " + explicitlySpecified)
            if (explicitlySpecified.isEmpty()) {
                explicitlySpecified.add("")
            }
            excluded = JavaConverters.asJavaCollectionConverter(ScalaJSTestResult$.MODULE$.lastSuccessfulClassnames).asJavaCollection().toSet()
                    .collect { it + objWildcard }
            project.logger.info("Excluding " + excluded)
            if (excluded.isEmpty()) {
                excluded.add("")
            }
        } else if (project.hasProperty(RETEST)) {
            explicitlySpecified = JavaConverters.asJavaCollectionConverter(ScalaJSTestResult$.MODULE$.lastFailedClassnames).asJavaCollection().toSet()
                    .collect { it + objWildcard }
            project.logger.info("Retesting " + explicitlySpecified)
            if (explicitlySpecified.isEmpty()) {
                explicitlySpecified.add("")
            }
        }
        scala.collection.immutable.Set<String> explicitlySpecifiedScala = JavaConverters.asScalaSet(explicitlySpecified).toSet()
        scala.collection.immutable.Set<String> excludedScala = JavaConverters.asScalaSet(excluded).toSet()

        Logger[] simpleLoggerArray = new SimpleLogger() as Logger[]
        JavaConverters.asJavaCollection(frameworksO).each { frameworkO ->
            if (frameworkO.nonEmpty()) {
                def framework = frameworkO.get()
                final Runner runner = framework.runner(new String[0], new String[0], null)
                final Fingerprint[] fingerprints = framework.fingerprints()
                final Task[] tasks = runner.tasks(ClassScanner.scan(classL, fingerprints, explicitlySpecifiedScala, excludedScala))
                project.logger.info("Executing " + framework.name())
                if (tasks.length == 0) {
                    project.logger.info("No tasks found")
                } else {
                    final ScalaJSTestStatus testStatus = new ScalaJSTestStatus(framework)
                    final EventHandler eventHandler = new ScalaJSEventHandler(testStatus)
                    ScalaJSTestResult$.MODULE$.statuses_$eq(ScalaJSTestResult.statuses().$plus(testStatus) as scala.collection.immutable.Set<ScalaJSTestStatus>)
                    tasks.each { Task t ->
                        t.execute(eventHandler, simpleLoggerArray)
                    }
                    project.logger.lifecycle('\n')
                    runner.done()
                    testStatus.finished_$eq(true)
                }
            }
        }

        project.logger.lifecycle(ScalaJSTestResult.toString())
        ScalaJSTestResult.save()

        //Make build fail
        if (!ScalaJSTestResult.isSuccess()) {
            throw new GradleException("There were failures while testing")
        }
    }
}
