package com.github.gtache

import com.github.gtache.tasks.CleanAllTask
import com.github.gtache.tasks.CompileJSTask
import com.github.gtache.tasks.RunJSTask
import com.github.gtache.tasks.TestJSTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaCompile

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
        project.dependencies.add('compile', 'org.scala-js:scalajs-library_2.11:0.6.8')
        project.dependencies.add('scalaCompilePlugin', 'org.scala-js:scalajs-compiler_2.11.8:0.6.8')
        project.logger.info('Dependencies added')
        final def jsDir = project.file('js/')
        final def jsFile = project.file(jsDir.path + '/' + project.name + '.js')
        final def jsFastFile = project.file(jsDir.path + '/' + project.name + '_fastopt.js')
        final def jsFullFile = project.file(jsDir.path + '/' + project.name + '_fullopt.js')

        final def runNoOpt = project.hasProperty('runNoOpt')
        final def runFull = project.hasProperty('runFull')

        if (runNoOpt && runFull) {
            project.logger.warn('runNoOpt and runFull both declared : Assuming runFull')
        }

        final def tasks = project.tasks;

        final def cleanAll = tasks.create('CleanAll', CleanAllTask.class)
        cleanAll.dependsOn('clean')
        cleanAll.toDelete = project.files(jsDir)
        project.logger.info('CleanAll task added')

        final def noOptJS = tasks.create('NoOptJS', CompileJSTask.class)
        noOptJS.dependsOn('classes')
        noOptJS.destFile = jsFile
        noOptJS.noOpt()
        project.logger.info('NoOptJS task added')

        final def fastOptJS = tasks.create('FastOptJS', CompileJSTask.class)
        fastOptJS.dependsOn('classes')
        fastOptJS.destFile = jsFastFile
        fastOptJS.fastOpt()
        project.logger.info('FastOptJS task added')

        final def fullOptJS = tasks.create('FullOptJS', CompileJSTask.class)
        fullOptJS.dependsOn('classes')
        fullOptJS.destFile = jsFullFile
        fullOptJS.fullOpt()
        project.logger.info('FullOptJS task added')

        final def runJS = tasks.create('RunJS', RunJSTask.class)

        final def testJS = tasks.create('TestJS', TestJSTask.class)
        testJS.dependsOn('testClasses')
        if (runFull) {
            testJS.dependsOn('FullOptJS')
            runJS.dependsOn('FullOptJS')
        } else if (runNoOpt) {
            testJS.dependsOn('NoOptJS')
            runJS.dependsOn('NoOptJS')
        } else {
            testJS.dependsOn('FastOptJS')
            runJS.dependsOn('FastOptJS')
        }
        project.logger.info('TestJS task added')

        project.afterEvaluate {
            tasks.withType(CompileJSTask) {
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
