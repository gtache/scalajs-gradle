package ch.epfl.gtache

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters

public class CompileJSTask extends DefaultTask {
    final String description = "Compiles all sjsir files into a single javascript file"
    @OutputFile
    File destFile
    Boolean fullOpt = false

    def fullOpt() {
        this.fullOpt = true
    }

    def fastOpt() {
        this.fullOpt = false
    }

    @TaskAction
    def exec() {
        FileCollection classpath = project.files(project.buildscript.configurations.getByName('classpath').asPath.split(';'))
        FileCollection cp = classpath + project.configurations.runtime + project.sourceSets.main.runtimeClasspath
        Scalajsld.Options options = Scalajsld.options().withOutput(destFile).withCp(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq())
        if (fullOpt) {
            options = options.withFullOpt()
        } else {
            options = options.withFastOpt()
        }
        Scalajsld.setOptions(options)
        Scalajsld.exec()
    }

}
