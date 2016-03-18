package ch.epfl.gtache

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaCompile;

class ScalajsPlugin implements Plugin<Project> {

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
        project.dependencies.add('compile', 'org.scala-js:scalajs-library_2.11:0.6.7')
        project.dependencies.add('scalaCompilePlugin', 'org.scala-js:scalajs-compiler_2.11.7:0.6.7')
        project.logger.info('Dependencies added')
        final def jsDir = project.file('js/')
        final def jsFile = project.file(jsDir.path + '/' + project.name + '.js')
        final def jsFastFile = project.file(jsDir.path + '/' + project.name + '_fastopt.js')
        final def jsFullFile = project.file(jsDir.path + '/' + project.name + '_fullopt.js')
        final def jsExecFile = project.file(jsDir.path + '/' + project.name + '_exec.js')
        final def jsFastExecFile = project.file(jsDir.path + '/' + project.name + '_fastopt_exec.js')
        final def jsFullExecFile = project.file(jsDir.path + '/' + project.name + '_fullopt_exec.js')

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

        final def createDirs = tasks.create('CreateDirs', CreateDirsTask.class)
        createDirs.toCreate = project.files(jsDir)
        project.logger.info('CreateDirs task added')

        final def noOptJS = tasks.create('NoOptJS', CompileJSTask.class)
        noOptJS.dependsOn('CreateDirs')
        noOptJS.dependsOn('classes')
        noOptJS.destFile = jsFile
        noOptJS.noOpt()
        project.logger.info('NoOptJS task added')

        final def fastOptJS = tasks.create('FastOptJS', CompileJSTask.class)
        fastOptJS.dependsOn('CreateDirs')
        fastOptJS.dependsOn('classes')
        fastOptJS.destFile = jsFastFile
        fastOptJS.fastOpt()
        project.logger.info('FastOptJS task added')

        final def fullOptJS = tasks.create('FullOptJS', CompileJSTask.class)
        fullOptJS.dependsOn('CreateDirs')
        fullOptJS.dependsOn('classes')
        fullOptJS.destFile = jsFullFile
        fullOptJS.fullOpt()
        project.logger.info('FullOptJS task added')

        final def copyJS = tasks.create('CopyJS', CopyJSTask.class)
        if (runFull) {
            copyJS.dependsOn('FullOptJS')
            copyJS.from(jsFullFile)
        } else if (runNoOpt) {
            copyJS.dependsOn('NoOptJS')
            copyJS.from(jsFile)
        } else {
            copyJS.dependsOn('FastOptJS')
            copyJS.from(jsFastFile)
        }
        copyJS.into(jsDir)
        project.logger.info('CopyJS task added')

        final def addMethExec = tasks.create('AddMethExec', AddMethExecTask.class)
        addMethExec.dependsOn('CopyJS')
        addMethExec.srcFile = runFull ? jsFullExecFile : (runNoOpt ? jsExecFile : jsFastExecFile)
        project.logger.info('AddMethExec task added')

        final def runJS = tasks.create('RunJS', RunJSTask.class)
        runJS.dependsOn('AddMethExec')
        runJS.toExec = runFull ? jsFullExecFile.absolutePath :
                (runNoOpt ? jsExecFile.absolutePath : jsFastExecFile.absolutePath)
        runJS.inferArgs()
        project.logger.info('RunJS task added')

        project.logger.info('ScalajsPlugin applied')

        project.afterEvaluate {
            project.logger.info('Configuring additional parameters related to Scalajs')
            tasks.withType(ScalaCompile) {
                scalaCompileOptions.additionalParameters = ["-Xplugin:" + project.configurations.scalaCompilePlugin.asPath]
            }
            project.logger.info('Xplugin for compiler added')
        }
    }

}
