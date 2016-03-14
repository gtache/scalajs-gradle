package ch.epfl.gtache

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

public class FastOptJSTask extends JavaExec {
    String description = "Compiles all sjsir files into a single javascript file"
    @OutputFile
    File destFile

    @Inject
    public FastOptJSTask(){
        this.main='Scalajsld'
        // workingDir = ?
        logger.info(workingDir.absolutePath)
    }
    @TaskAction
    def fastOptJS() {
        classpath = project.configurations.runtime
        def srcDir = project.sourceSets.main.runtimeClasspath
        classpath += srcDir
        inputs.files(srcDir)
        def argsL = new ArrayList<String>()
        argsL.add(srcDir.absolutePath)
        argsL.add(destFile.absolutePath)
        classpath.each { argsL.add(it.absolutePath) }
        args = argsL
    }

}
