package com.github.gtache.tasks

import com.github.gtache.Scalajsld
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import scala.Option
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
        Scalajsld.Options curOptions = Scalajsld.options()
        def options = parseOptions()
        if (!options.equals(curOptions)) {
            Scalajsld.setOptions(options)
            logger.debug('Options changed, linker and cache recreated')
        }
        Scalajsld.exec()
    }

    /**
     * Configure the options given the project properties (given by user)
     * @return The configured options
     */
    private def Scalajsld.Options parseOptions() {
        FileCollection classpath = project.files(project.buildscript.configurations.getByName('classpath').asPath.split(';'))
        FileCollection cp = classpath + project.configurations.runtime + project.sourceSets.main.runtimeClasspath
        Scalajsld.Options options = Scalajsld.defaultOptions().withClasspath(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq())

        if (project.hasProperty('o')) {
            options = options.withOutput(project.file(project.property('o')))
        } else if (project.hasProperty('output')) {
            options = options.withOutput(project.file(project.property('output')))
        } else {
            options = options.withOutput(destFile)
        }

        if (fullOpt) {
            options = options.withFullOpt()
        } else if (noOpt) {
            options = options.withNoOpt()
        } else {
            options = options.withFastOpt()
        }

        if (project.hasProperty('p') || project.hasProperty('prettyPrint')) {
            options = options.withPrettyPrint(true)
        }

        if (project.hasProperty('s') || project.hasProperty('sourceMap')) {
            options = options.withSourceMap(true)
        }

        if (project.hasProperty('compliantAsInstanceOfs')) {
            options = options.withCompliantsSemantics()
        }

        if (project.hasProperty('m')) {
            options = options.withOutputMode((OutputMode) project.property('m'))
        } else if (project.hasProperty('outputMode')) {
            options = options.withOutputMode((OutputMode) project.property('outputMode'))
        }

        if (project.hasProperty('c') || project.hasProperty('checkIR')) {
            options = options.withCheckIR(true)
        }

        if (project.hasProperty('r')) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property('r'))))
        } else if (project.hasProperty('relativizeSourceMap')) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property('relativizeSourceMap'))))
        }

        if (project.hasProperty('linkLogLevel')) {
            switch (project.property('linkLogLevel')) {
                case 'Error':
                    options = options.withLogLevel(Level.Error$.MODULE$)
                    break
                case 'Warn':
                    options = options.withLogLevel(Level.Warn$.MODULE$)
                    break
                case 'Info':
                    options = options.withLogLevel(Level.Info$.MODULE$)
                    break
                case 'Debug':
                    options = options.withLogLevel(Level.Debug$.MODULE$)
                    break
                default:
                    logger.warn("Unknown log level : " + project.property('linkLogLevel'))
                    break
            }
        } else if (project.hasProperty('d') || project.hasProperty('debug')) {
            options = options.withLogLevel(Level.Debug$.MODULE$)
        } else if (project.hasProperty('q') || project.hasProperty('quiet')) {
            options = options.withLogLevel(Level.Warn$.MODULE$)
        } else if (project.hasProperty('qq') || project.hasProperty('really-quiet')) {
            options = options.withLogLevel(Level.Error$.MODULE$)
        }

        logger.info('Running linker with ' + options.toString())

        return options
    }

}
