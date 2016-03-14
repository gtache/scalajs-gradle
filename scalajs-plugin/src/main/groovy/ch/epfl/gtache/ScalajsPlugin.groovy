package ch.epfl.gtache

import org.gradle.api.Plugin
import org.gradle.api.Project

class ScalajsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.logger.info('Applying java plugin')
        project.apply(plugin : 'java')
        project.logger.info('Applying scala plugin')
        project.apply(plugin : 'scala')
        project.logger.info('Plugins applied')
        project.logger.info('Adding scalajs-library and scalajs-compiler dependencies')
        project.dependencies.add('compile','org.scala-js:scalajs-library_2.11:0.6.7')
        project.dependencies.add('compile','org.scala-js:scalajs-compiler_2.11.7:0.6.7')
        project.logger.info('Dependencies added')
        final def jsDir = project.file('js/')
        final def jsFile = project.file(jsDir.path + project.name + '.js')
        final def jsExecFile = project.file(jsDir.path + project.name + '_exec.js')

        final def tasks = project.tasks;

        final def cleanAll = tasks.create('CleanAll', CleanAllTask.class)
        cleanAll.dependsOn('clean')
        cleanAll.toDelete = project.files(jsDir)
        project.logger.info('CleanAll task added')

        final def createDirs = tasks.create('CreateDirs', CreateDirsTask.class)
        createDirs.toCreate = project.files(jsDir)
        project.logger.info('CreateDirs task added')

        final def fastOptJS = tasks.create('FastOptJS', FastOptJSTask.class)
        fastOptJS.dependsOn('CreateDirs')
        fastOptJS.dependsOn('classes')
        fastOptJS.destFile = jsFile
        project.logger.info('FastOptJS task added')

        final def copyJS = tasks.create('CopyJS', CopyJSTask.class)
        copyJS.dependsOn('FastOptJS')
        copyJS.from(jsFile)
        copyJS.into(jsExecFile)
        project.logger.info('CopyJS task added')

        final def addMethExec = tasks.create('AddMethExec', AddMethExecTask.class)
        addMethExec.dependsOn('CopyJS')
        addMethExec.srcFile = jsExecFile
        project.logger.info('AddMethExec task added')

        final def runJS = tasks.create('RunJS', RunJSTask.class)
        runJS.dependsOn('AddMethExec')
        runJS.toExec = jsExecFile
        project.logger.info('RunJS task added')

        project.logger.info('ScalajsPlugin applied')
    }
}
