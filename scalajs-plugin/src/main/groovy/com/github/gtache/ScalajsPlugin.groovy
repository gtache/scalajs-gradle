package com.github.gtache

import com.github.gtache.tasks.CompileJSTask
import com.github.gtache.tasks.RunJSTask
import com.github.gtache.tasks.TestJSTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaCompile

import static com.github.gtache.Utils.*

/**
 * The main class for the plugin
 */
public final class ScalajsPlugin implements Plugin<Project> {

    /**
     * Applies the plugin to the given project
     * @param project The project it will apply the plugin to
     */
    @Override
    public void apply(Project project) {
        project.logger.info('Applying java plugin')
        project.pluginManager.apply('java')
        project.logger.info('Applying scala plugin')
        project.pluginManager.apply('scala')
        project.logger.info('Plugins applied')

        project.configurations {
            scalaCompilePlugin
        }

        project.logger.info('Adding scalajs-library and scalajs-compiler dependencies')
        project.dependencies.add('compile', 'org.scala-js:scalajs-library_'
                + SCALA_VERSION + ':' + SCALAJS_VERSION)
        project.dependencies.add('scalaCompilePlugin', 'org.scala-js:scalajs-compiler_'
                + SCALA_VERSION + '.' + COMPILER_VERSION + ':' + SCALAJS_VERSION)
        project.logger.info('Dependencies added')

        final def jsDir = project.file(project.buildDir.absolutePath + JS_REL_DIR)
        final def jsBaseName = jsDir.absolutePath + File.separator + project.name
        final def jsFile = project.file(jsBaseName + EXT)
        final def jsTestFile = project.file(jsBaseName + NOOPT_TEST_SUFFIX)
        final def jsFastFile = project.file(jsBaseName + FASTOPT_SUFFIX)
        final def jsTestFastFile = project.file(jsBaseName + FASTOPT_TEST_SUFFIX)
        final def jsFullFile = project.file(jsBaseName + FULLOPT_SUFFIX)
        final def jsTestFullFile = project.file(jsBaseName + FULLOPT_TEST_SUFFIX)

        final def runNoOpt = project.hasProperty(RUN_NOOPT)
        final def runFull = project.hasProperty(RUN_FULL)

        if (runNoOpt && runFull) {
            project.logger.warn(RUN_NOOPT + ' and ' + RUN_FULL + ' both declared : Assuming ' + RUN_FULL)
        }

        final def tasks = project.tasks;

        final def noOptJS = tasks.create('NoOptJS', CompileJSTask.class)
        noOptJS.destFile = jsFile
        noOptJS.noOpt()
        project.logger.info(noOptJS.name + ' task added')

        final def fastOptJS = tasks.create('FastOptJS', CompileJSTask.class)
        fastOptJS.destFile = jsFastFile
        fastOptJS.fastOpt()
        project.logger.info(fastOptJS.name + ' task added')

        final def fullOptJS = tasks.create('FullOptJS', CompileJSTask.class)
        fullOptJS.destFile = jsFullFile
        fullOptJS.fullOpt()
        project.logger.info(fullOptJS.name + ' task added')

        final def runJS = tasks.create('RunJS', RunJSTask.class)

        final def classes = 'classes'
        final def testClasses = 'testClasses'

        final def testJS = tasks.create('TestJS', TestJSTask.class)
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

        project.afterEvaluate {
            tasks.withType(CompileJSTask) {
                it.dependsOn(classes)
                it.mustRunAfter(testClasses, classes)
                if (checkTaskInStartParameter(project, testJS.name)) {
                    project.logger.info('IN STARTPARAMETER')
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
                } else {
                    project.logger.info('NOT IN STARTPARAMETER')
                    it.srcFiles = project.files(project.sourceSets.main.runtimeClasspath)
                }
                it.configure()
            }
            tasks.withType(ScalaCompile) {
                scalaCompileOptions.additionalParameters = ["-Xplugin:" + project.configurations.scalaCompilePlugin.asPath]
            }
            project.logger.info('Xplugin for compiler added')
            project.logger.info('ScalajsPlugin applied')
        }
    }

}
