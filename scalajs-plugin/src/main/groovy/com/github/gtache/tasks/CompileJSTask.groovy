package com.github.gtache.tasks

import com.github.gtache.Scalajsld
import com.github.gtache.Utils
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
    def run() {
        final def curOptions = Scalajsld.options()
        final def options = parseOptions()
        if (!options.equals(curOptions)) {
            Scalajsld.setOptions(options)
            logger.debug('Options changed, linker recreated')
        }
        Scalajsld.exec()
    }

    /**
     * Configure the options given the project properties (given by user)
     * @return The configured options
     */
    private def Scalajsld.Options parseOptions() {
        final def classpath = project.files(project.buildscript.configurations.getByName('classpath').asPath.split(';'))
        final def cp = classpath + project.configurations.runtime + project.sourceSets.main.runtimeClasspath
        def options = Scalajsld.defaultOptions().withClasspath(
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
            String modeS = project.property('m')
            OutputMode mode = Utils.getOutputMode(modeS)
            if (mode != null) {
                options = options.withOutputMode(mode)
            } else {
                logger.error("Unknown outputMode")
            }
        } else if (project.hasProperty('outputMode')) {
            String modeS = project.property('outputMode')
            OutputMode mode = Utils.getOutputMode(modeS)
            if (mode != null) {
                options = options.withOutputMode(mode)
            } else {
                logger.error("Unknown outputMode")
            }
        }

        if (project.hasProperty('c') || project.hasProperty('checkIR')) {
            options = options.withCheckIR(true)
        }

        if (project.hasProperty('r')) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property('r'))))
        } else if (project.hasProperty('relativizeSourceMap')) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property('relativizeSourceMap'))))
        }

        Level level = Utils.resolveLogLevel(project, 'linkLogLevel', Level.Info$.MODULE$)
        if (project.hasProperty('d') || project.hasProperty('debug')) {
            level = Level.Debug$.MODULE$
        } else if (project.hasProperty('q') || project.hasProperty('quiet')) {
            level = Level.Warn$.MODULE$
        } else if (project.hasProperty('qq') || project.hasProperty('really-quiet')) {
            level = Level.Error$.MODULE$
        }
        if (level != Level.Info$.MODULE$) {
            options = options.withLogLevel(level)
        }

        logger.info('Running linker with ' + options.toString())

        return options
    }

}
