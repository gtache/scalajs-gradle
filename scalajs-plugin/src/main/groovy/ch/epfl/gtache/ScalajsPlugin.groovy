package ch.epfl.gtache

import org.gradle.api.Plugin
import org.gradle.api.Project

class ScalajsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final def sjsirDir = project.file("sjsir/")
        final def jsDir = project.file("js/")
        final def jsFile = project.file(jsDir + project.name + ".js")
        final def jsExecFile = project.file(jsDir + project.name + "_exec.js")

        final def tasks = project.tasks;

        final def cleanAll = tasks.create("CleanAll", CleanAllTask.class)
        cleanAll.dependsOn("clean")
        cleanAll.toDelete = project.files(sjsirDir, jsDir)

        final def createDirs = tasks.create("CreateDirs", CreateDirsTask.class)
        createDirs.toCreate = project.files(sjsirDir, jsDir)

        final def fastOptJS = tasks.create("FastOptJS", FastOptJSTask.class)
        fastOptJS.dependsOn("compileScala")
        fastOptJS.srcDir = sourceSets.main.runtimeClasspath
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
