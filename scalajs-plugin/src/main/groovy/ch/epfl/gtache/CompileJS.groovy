package ch.epfl.gtache

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

public class CompileJSTask extends JavaExec {
    String description = "Compiles all sjsir files into a single javascript file"
    @OutputFile
    File destFile
    String fullOpt = 'f'

    public CompileJSTask() {
        this.main = 'ch.epfl.gtache.Scalajsld'
    }

    def fullOpt() {
        this.fullOpt = 't'
    }

    def fastOpt() {
        this.fullOpt = 'f'
    }

    def finishConfiguration() {
        classpath += project.configurations.runtime
        classpath += project.sourceSets.main.runtimeClasspath
        def argsL = new ArrayList<String>()
        argsL.add(fullOpt)
        argsL.add(destFile.absolutePath)
        classpath.each { argsL.add(it.absolutePath) }
        args = argsL
    }

}
