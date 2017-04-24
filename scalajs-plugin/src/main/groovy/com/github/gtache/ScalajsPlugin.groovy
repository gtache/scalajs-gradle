package com.github.gtache

import com.github.gtache.tasks.CompileJSTask
import com.github.gtache.tasks.RunJSTask
import com.github.gtache.tasks.ScalajspTask
import com.github.gtache.tasks.TestJSTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.scala.ScalaCompile

import static com.github.gtache.BuildConfig.*
import static com.github.gtache.Utils.*
import static com.github.gtache.tasks.CompileJSTask.*
import static com.github.gtache.tasks.ScalajspTask.*

/**
 * The main class for the plugin
 */
final class ScalajsPlugin implements Plugin<Project> {

    /**
     * Applies the plugin to the given project
     * @param project The project it will apply the plugin to
     */
    @Override
    void apply(Project project) {
        prepareGraph(project)
        warnConflictingProperties(project)
        project.logger.info('Applying java plugin')
        project.pluginManager.apply('java')
        project.logger.info('Applying scala plugin')
        project.pluginManager.apply('scala')
        project.logger.info('Plugins applied')

        project.configurations {
            scalaCompilePlugin
            phantomJetty
        }

        project.logger.info('Adding scalajs-library and scalajs-compiler dependencies')
        project.dependencies.add('compile', 'org.scala-js:scalajs-library_'
                + SCALA_VERSION + ':' + SCALAJS_VERSION)
        project.dependencies.add('scalaCompilePlugin', 'org.scala-js:scalajs-compiler_'
                + SCALA_FULL_VERSION + ':' + SCALAJS_VERSION)
        project.logger.info('Adding jetty dependencies')
        project.dependencies.add('phantomJetty', 'org.eclipse.jetty:jetty-server:' + JETTY_SERVER_VERSION)
        project.dependencies.add('phantomJetty', 'org.eclipse.jetty:jetty-websocket:' + JETTY_WEBSOCKET_VERSION)
        project.logger.info('Dependencies added')

        final jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        final jsBaseName = jsDir.absolutePath + File.separator + project.name
        final jsFile = project.file(jsBaseName + EXT)
        final jsTestFile = project.file(jsBaseName + NOOPT_TEST_SUFFIX)
        final jsFastFile = project.file(jsBaseName + FASTOPT_SUFFIX)
        final jsTestFastFile = project.file(jsBaseName + FASTOPT_TEST_SUFFIX)
        final jsFullFile = project.file(jsBaseName + FULLOPT_SUFFIX)
        final jsTestFullFile = project.file(jsBaseName + FULLOPT_TEST_SUFFIX)

        final runNoOpt = project.hasProperty(RUN_NOOPT)
        final runFull = project.hasProperty(RUN_FULL)


        final tasks = project.tasks

        final noOptJS = tasks.create('NoOptJS', CompileJSTask.class)
        noOptJS.destFile = jsFile
        noOptJS.noOpt()
        project.logger.info(noOptJS.name + ' task added')

        final fastOptJS = tasks.create('FastOptJS', CompileJSTask.class)
        fastOptJS.destFile = jsFastFile
        fastOptJS.fastOpt()
        project.logger.info(fastOptJS.name + ' task added')

        final fullOptJS = tasks.create('FullOptJS', CompileJSTask.class)
        fullOptJS.destFile = jsFullFile
        fullOptJS.fullOpt()
        project.logger.info(fullOptJS.name + ' task added')

        final runJS = tasks.create('RunJS', RunJSTask.class)

        final classes = 'classes'
        final testClasses = 'testClasses'

        final testJS = tasks.create('TestJS', TestJSTask.class)
        testJS.dependsOn(testClasses)

        if (runFull) {
            testJS.dependsOn(fullOptJS)
            runJS.dependsOn(fullOptJS)
        } else if (runNoOpt) {
            testJS.dependsOn(noOptJS)
            runJS.dependsOn(noOptJS)
        } else {
            testJS.dependsOn(fastOptJS)
            runJS.dependsOn(fastOptJS)
        }
        project.logger.info(testJS.name + ' task added')

        final scalajsp = tasks.create('Scalajsp', ScalajspTask.class)
        project.logger.info(scalajsp.name + ' task added')

        project.afterEvaluate {
            tasks.withType(CompileJSTask) {
                it.dependsOn(classes)
                it.mustRunAfter(testClasses, classes)
                it.srcFiles = project.files(project.sourceSets.main.runtimeClasspath)
                it.configure()
            }
            project.gradle.taskGraph.whenReady { TaskExecutionGraph graph ->
                if (graph.hasTask(testJS)) {
                    tasks.withType(CompileJSTask) {
                        if (it == fastOptJS) {
                            it.destFile = jsTestFastFile
                        } else if (it == fullOptJS) {
                            it.destFile = jsTestFullFile
                        } else if (it == noOptJS) {
                            it.destFile = jsTestFile
                        } else {
                            throw new IllegalStateException("Unknown task : " + it.name)
                        }
                        it.srcFiles = project.files(project.sourceSets.test.runtimeClasspath)
                        it.configure()
                    }
                }
            }
            tasks.withType(ScalaCompile) {
                def existingParameters = scalaCompileOptions.additionalParameters ?: []
                scalaCompileOptions.additionalParameters = existingParameters + ["-Xplugin:" + project.configurations.scalaCompilePlugin.findAll {
                    it.absolutePath.contains('scalajs-compiler')
                }.get(0).absolutePath]
            }
            project.logger.info('Xplugin for compiler added')
            project.logger.info('ScalajsPlugin applied')
        }
    }

    /**
     * Warns the user if conflicting parameters are set for the given project
     * @param project the project
     */
    private static void warnConflictingProperties(Project project) {
        Set<List<String>> linkedProperties = new HashSet<>()
        List<String> opt = new ArrayList<>()
        List<String> envs = new ArrayList<>()
        List<String> output = new ArrayList<>()
        List<String> outputMode = new ArrayList<>()
        List<String> relSM = new ArrayList<>()
        List<String> logLevel = new ArrayList<>()
        List<String> filenames = new ArrayList<>()
        List<String> jar = new ArrayList<>()


        opt.add(RUN_FULL)
        opt.add(RUN_NOOPT)
        envs.add(JSENV)
        envs.add(RHINO)
        envs.add(PHANTOM)
        envs.add(JSDOM)
        output.add(MIN_OUTPUT)
        output.add(OUTPUT)
        outputMode.add(MIN_OUTPUTMODE)
        outputMode.add(OUTPUT)
        relSM.add(MIN_RELSM)
        relSM.add(RELSM)
        logLevel.add(MIN_DEBUG)
        logLevel.add(DEBUG)
        logLevel.add(MIN_WARN)
        logLevel.add(WARN)
        logLevel.add(MIN_ERR)
        logLevel.add(ERR)
        logLevel.add(LOG_LEVEL)
        filenames.add(MIN_FILENAME)
        filenames.add(FILENAME)
        jar.add(MIN_JAR)
        jar.add(JAR)

        linkedProperties.add(opt)
        linkedProperties.add(envs)
        linkedProperties.add(output)
        linkedProperties.add(outputMode)
        linkedProperties.add(relSM)
        linkedProperties.add(logLevel)
        linkedProperties.add(filenames)
        linkedProperties.add(jar)

        for (List<String> l : linkedProperties) {
            Set<Integer> declared = new HashSet<>()
            int shortestIndex = -1
            for (int i = 0; i < l.size(); ++i) {
                if (project.hasProperty(l.get(i))) {
                    if (shortestIndex < 0) {
                        shortestIndex = i
                    }
                    declared.add(i)
                }
            }
            if (declared.size() > 1) {
                String message = declared.collect { x -> l.get(x) }.inject({ acc, word -> acc + ', ' + word })
                message = "Declaring " + message + " ; Assuming " + l.get(shortestIndex)
                project.logger.warn(message)
            }
        }
    }
}

