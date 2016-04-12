package com.github.gtache

import com.github.gtache.tasks.CleanAllTask
import com.github.gtache.tasks.CompileJSTask
import com.github.gtache.tasks.RunJSTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaCompile

/**
 * The main class for the plugin
 */
class ScalajsPlugin implements Plugin<Project> {

    /**
     * Applies the plugin to the given project
     */
    @Override
    public void apply(Project project) {
        project.logger.info('Applying java plugin')
        project.apply(plugin: 'java')
        project.logger.info('Applying scala plugin')
        project.apply(plugin: 'scala')
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

        if (runFull) {
            runJS.dependsOn('FullOptJS')
        } else if (runNoOpt) {
            runJS.dependsOn('NoOptJS')
        } else {
            runJS.dependsOn('FastOptJS')
        }
        project.logger.info('ScalajsPlugin applied')

        project.afterEvaluate {
            project.logger.info('Configuring additional parameters related to Scalajs')
            tasks.withType(ScalaCompile) {
                scalaCompileOptions.additionalParameters = ["-Xplugin:" + project.configurations.scalaCompilePlugin.asPath]
            }
            tasks.withType(CompileJSTask) {
                it.srcFiles = project.files(project.sourceSets.main.runtimeClasspath)
            }
            project.logger.info('Xplugin for compiler added')
        }
    }

}
