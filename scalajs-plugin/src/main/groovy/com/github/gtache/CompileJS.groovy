package com.github.gtache

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters

/**
 * Task used to compile sjsir and classes file to a js file.
 */
public class CompileJSTask extends DefaultTask {
    final String description = "Compiles all sjsir files into a single javascript file"
    @OutputFile
    File destFile
    @InputFiles
    FileCollection srcFiles
    Boolean fullOpt = false
    Boolean noOpt = false

    /**
     * Tells Scalajsld to run with full optimization
     */
    def fullOpt() {
        this.fullOpt = true
        this.noOpt = false
    }

    /**
     * Tells Scalajsld to run with fast optimization
     */
    def fastOpt() {
        this.fullOpt = false
        this.noOpt = false
    }

    /**
     * Tells Scalajsld to run with no optimization
     */
    def noOpt() {
        this.fullOpt = false
        this.noOpt = true
    }

    /**
     * Main method of the task, configures and runs Scalajsld
     */
    @TaskAction
    def exec() {
        String separator = System.getProperty("path.separator")
        FileCollection classpath = project.files(project.buildscript.configurations.getByName('classpath').asPath.split(separator))
        FileCollection cp = classpath + project.configurations.runtime + project.sourceSets.main.runtimeClasspath
        Scalajsld.Options curOptions = Scalajsld.options()
        Scalajsld.Options options = curOptions.withOutput(destFile).withClasspath(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq())
        if (fullOpt) {
            options = options.withFullOpt()
        } else if (noOpt) {
            options = options.withNoOpt()
        } else {
            options = options.withFastOpt()
        }
        if (!options.equals(curOptions)) {
            Scalajsld.setOptions(options)
        }
        Scalajsld.exec()
    }

}
