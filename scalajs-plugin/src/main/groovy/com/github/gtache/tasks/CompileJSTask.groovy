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
import org.scalajs.core.tools.sem.Semantics
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
    public static final String MIN_N_SOURCEMAP = 'noS'
    public static final String N_SOURCEMAP = 'noSourceMap'
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
    public static final String SEMANTICS = 'semantics'
    public static final String NO_PARALLEL = 'noParallel'
    public static final String BATCH = 'batch'
    public static final String OPTIONS = 'oOptions'


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
     * Configure the options given the project properties (given by user)
     */
    def configure() {
        options = parseOptions()
    }

    /**
     * Main method of the task, configures and runs Scalajsld
     */
    @TaskAction
    def run() {
        final curOptions = Scalajsld.getOptions()
        if (options != curOptions) {
            Scalajsld.setOptions(options)
            logger.info('Options changed, linker recreated')
        }
        logger.info('Running linker with ' + options.toString())

        Scalajsld.exec()
    }

    /**
     * Parse the options given the project properties
     * @return The configured options
     */
    private Scalajsld.Options parseOptions() {
        final cp = project.configurations.runtimeClasspath + srcFiles
        def options = Scalajsld.defaultOptions().withClasspath(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq())


        if (project.hasProperty(OPTIONS)) {
            def optimizerOptions = project.property(OPTIONS)
            if (optimizerOptions instanceof Scalajsld.Options) {
                options = options.withOptimizerOptions(optimizerOptions as Scalajsld.Options)
            } else {
                project.error("OptimizerOptions are not of the class Scalajsld.Options : was " + project.property(OPTIONS).getClass())
            }
        }
        if (project.hasProperty(MIN_OUTPUT)) {
            destFile = project.file(project.property(MIN_OUTPUT))
        } else if (project.hasProperty(OUTPUT)) {
            destFile = project.file(project.property(OUTPUT))
        }
        options = options.withOutput(destFile)

        options = options.withModuleKind(Utils.resolveModuleKind(project))

        if (fullOpt) {
            options = options.withUseClosureCompiler(true)
        } else if (noOpt) {
            options = options.withDisableOptimizer(true)
        } else {
            options = options.withDisableOptimizer(false)
        }

        if (project.hasProperty(MIN_PRETTY) || project.hasProperty(PRETTY)) {
            options = options.withPrettyPrint(true)
        }

        if (project.hasProperty(MIN_N_SOURCEMAP) || project.hasProperty(N_SOURCEMAP)) {
            options = options.withSourceMap(false)
        }

        if (project.hasProperty(COMPLIANT)) {
            options = options.withCompliantsSemantics()
        }

        if (project.hasProperty(SEMANTICS)) {
            def semanticsObj = project.property(SEMANTICS)
            if (semanticsObj instanceof Semantics) {
                options = options.withSemantics(semanticsObj as Semantics)
            } else {
                project.logger.error("The object given as \"semantics\" is not of type Semantics : was " + semanticsObj.getClass())
            }

        } else if (fullOpt) {
            options = options.withSemantics(options.semantics().withProductionMode(true))
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

        if (project.hasProperty(BATCH)) {
            options = options.withBatchMode(true)
        }
        if (project.hasProperty(NO_PARALLEL)) {
            options = options.withParallel(false)
        }


        return options
    }

}
