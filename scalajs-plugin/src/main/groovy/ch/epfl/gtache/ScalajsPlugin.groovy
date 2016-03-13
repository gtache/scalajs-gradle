package ch.epfl.gtache

import org.gradle.api.Plugin
import org.gradle.api.Project

class ScalajsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.apply(plugin : 'java')
        project.dependencies.add('compile','org.scala-js:scalajs-library_2.11.7:0.6.7')
        project.dependencies.add('compile','org.scala-js:scalajs-compiler_2.11.7:0.6.7')
        final def jsDir = project.file("js/")
        final def jsFile = project.file(jsDir + project.name + ".js")
        final def jsExecFile = project.file(jsDir + project.name + "_exec.js")

        final def tasks = project.tasks;

        final def cleanAll = tasks.create("CleanAll", CleanAllTask.class)
        cleanAll.dependsOn("clean")
        cleanAll.toDelete = project.files(jsDir)

        final def createDirs = tasks.create("CreateDirs", CreateDirsTask.class)
        createDirs.toCreate = project.files(jsDir)

        final def fastOptJS = tasks.create("FastOptJS", FastOptJSTask.class)
        fastOptJS.dependsOn("compileScala")
        fastOptJS.srcDir = project.sourceSets.main.runtimeClasspath
        fastOptJS.destFile = jsFile

        final def copyJS = tasks.create("CopyJS", CopyJSTask.class)
        copyJS.dependsOn("FastOptJS")
        copyJS.from(jsFile)
        copyJS.into(jsExecFile)

        final def addMethExec = tasks.create("AddMethExec", AddMethExecTask.class)
        addMethExec.dependsOn("CopyJS")
        addMethExec.srcFile = jsExecFile

        final def runJS = tasks.create("RunJS", RunJSTask.class)
        runJS.dependsOn("AddMethExec")
        runJS.toExec = jsExecFile
    }
}
