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

    //Linker configs
    public static final String MIN_OUTPUT = 'o'
    public static final String OUTPUT = 'output'
    public static final String MIN_PRETTY = 'p'
    public static final String PRETTY = 'prettyPrint'
    public static final String MIN_SOURCEMAP = 's'
    public static final String SOURCEMAP = 'sourceMap'
    public static final String COMPLIANT = 'compliantAsInstanceOfs'
    public static final String MIN_OUTPUTMODE = 'm'
    public static final String OUTPUTMODE = 'outputMode'
    public static final String MIN_CHECKIR = 'c'
    public static final String CHECKIR = 'checkIR'
    public static final String MIN_RELSM = 'r'
    public static final String RELSM = 'relativizeSourceMap'
    public static final String LOG_LEVEL = 'linkLogLevel'
    public static final String MIN_DEBUG = 'd'
    public static final String DEBUG = 'debug'
    public static final String MIN_WARN = 'q'
    public static final String WARN = 'quiet'
    public static final String MIN_ERR = 'qq'
    public static final String ERR = 'really-quiet'

    private Scalajsld.Options options
    @InputFiles
    FileCollection srcFiles
    @OutputFile
    File destFile
    private Boolean fullOpt = false
    private Boolean noOpt = false

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
     * Parse the options
     * @return
     */
    def configure() {
        options = parseOptions()
    }

    /**
     * Main method of the task, configures and runs Scalajsld
     */
    @TaskAction
    def run() {
        final def curOptions = Scalajsld.options()
        if (!options.equals(curOptions)) {
            Scalajsld.setOptions(options)
            logger.info('Options changed, linker recreated')
        }
        logger.info('Running linker with ' + options.toString())
        Scalajsld.exec()
    }

    /**
     * Returns the options that will be used with the linker
     * @return The options
     */
    public Scalajsld.Options getOptions() {
        return options
    }

    /**
     * Configure the options given the project properties (given by user)
     * @return The configured options
     */
    private def Scalajsld.Options parseOptions() {
        final
        def classpath = project.files(project.buildscript.configurations.getByName('classpath').asPath.split(File.pathSeparator))
        final def cp = classpath + project.configurations.runtime + srcFiles
        def options = Scalajsld.defaultOptions().withClasspath(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq())


        if (project.hasProperty(MIN_OUTPUT)) {
            destFile = project.file(project.property(MIN_OUTPUT))
        } else if (project.hasProperty(OUTPUT)) {
            destFile = project.file(project.property(OUTPUT))
        }
        options = options.withOutput(destFile)

        if (fullOpt) {
            options = options.withFullOpt()
        } else if (noOpt) {
            options = options.withNoOpt()
        } else {
            options = options.withFastOpt()
        }

        if (project.hasProperty(MIN_PRETTY) || project.hasProperty(PRETTY)) {
            options = options.withPrettyPrint(true)
        }

        if (project.hasProperty(MIN_SOURCEMAP) || project.hasProperty(SOURCEMAP)) {
            options = options.withSourceMap(true)
        }

        if (project.hasProperty(COMPLIANT)) {
            options = options.withCompliantsSemantics()
        }


        if (project.hasProperty(MIN_OUTPUTMODE)) {
            String modeS = project.property(MIN_OUTPUTMODE)
            OutputMode mode = Utils.getOutputMode(modeS)
            if (mode != null) {
                options = options.withOutputMode(mode)
            } else {
                logger.error("Unknown output mode")
            }
        } else if (project.hasProperty(OUTPUTMODE)) {
            String modeS = project.property(OUTPUTMODE)
            OutputMode mode = Utils.getOutputMode(modeS)
            if (mode != null) {
                options = options.withOutputMode(mode)
            } else {
                logger.error("Unknown output mode")
            }
        }

        if (project.hasProperty(MIN_CHECKIR) || project.hasProperty(CHECKIR)) {
            options = options.withCheckIR(true)
        }


        if (project.hasProperty(MIN_RELSM)) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property(MIN_RELSM))))
        } else if (project.hasProperty(RELSM)) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property(RELSM))))
        }

        Level level = Utils.resolveLogLevel(project, LOG_LEVEL, Level.Info$.MODULE$)
        if (project.hasProperty(MIN_DEBUG) || project.hasProperty(DEBUG)) {
            level = Level.Debug$.MODULE$
        } else if (project.hasProperty(MIN_WARN) || project.hasProperty(WARN)) {
            level = Level.Warn$.MODULE$
        } else if (project.hasProperty(MIN_ERR) || project.hasProperty(ERR)) {
            level = Level.Error$.MODULE$
        }
        if (level != Level.Info$.MODULE$) {
            options = options.withLogLevel(level)
        }


        return options
    }

}
