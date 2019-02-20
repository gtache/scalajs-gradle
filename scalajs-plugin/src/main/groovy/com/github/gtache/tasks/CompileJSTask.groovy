package com.github.gtache.tasks

import com.github.gtache.Scalajsld
import com.github.gtache.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.scalajs.core.tools.linker.ModuleInitializer
import org.scalajs.core.tools.linker.backend.ModuleKind
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.linker.backend.OutputMode$
import org.scalajs.core.tools.logging.Level
import org.scalajs.core.tools.sem.Semantics
import scala.Option
import scala.collection.JavaConverters
import scala.collection.Seq

/**
 * Task used to compile sjsir and classes file to a js file.
 */
public class CompileJSTask extends DefaultTask {
    final String description = "Compiles all sjsir files into a single javascript file"

    //TODO also update scalajs-plugin-test while at it
    //Linker configs
    public static final String MODULE_INITIALIZERS = 'moduleInitializers'
    public static final String MIN_OUTPUT = 'o'
    public static final String OUTPUT = 'output'
    public static final String SEMANTICS = 'semantics'
    public static final String ES_FEATURES = 'esFeatures'
    public static final String MODULE_KIND = 'moduleKind'
    public static final String COMPLIANT = 'compliantAsInstanceOfs'
    public static final String MIN_PRETTY = 'p'
    public static final String PRETTY = 'prettyPrint'
    public static final String MIN_N_SOURCEMAP = 'noS'
    public static final String N_SOURCEMAP = 'noSourceMap'
    public static final String MIN_RELSM = 'r'
    public static final String RELSM = 'relativizeSourceMap'
    public static final String BATCH = 'batch'
    public static final String NO_PARALLEL = 'noParallel'
    public static final String MIN_CHECKIR = 'c'
    public static final String CHECKIR = 'checkIR'
    public static final String STDLIB = 'stdLib'
    public static final String MIN_DEBUG = 'd'
    public static final String DEBUG = 'debug'
    public static final String MIN_WARN = 'q'
    public static final String WARN = 'quiet'
    public static final String MIN_ERR = 'qq'
    public static final String ERR = 'really-quiet'
    public static final String LOG_LEVEL = 'linkLogLevel'

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
     * Returns the options that will be used with the linker
     * @return The options
     */
    public Scalajsld.Options getOptions() {
        return options
    }

    /**
     * Parse the options given the project properties
     * @return The configured options
     */
    private Scalajsld.Options parseOptions() {
        final cp = project.configurations.runtime + srcFiles
        def options = Scalajsld.defaultOptions().withClasspath(
                JavaConverters.asScalaSetConverter(cp.getFiles()).asScala().toSet().toSeq() as Seq<File>)


        if (project.hasProperty(OPTIONS)) {
            def optimizerOptions = project.property(OPTIONS)
            if (optimizerOptions instanceof Scalajsld.Options) {
                options = options.withOptimizerOptions(optimizerOptions)
            } else {
                project.error("OptimizerOptions not of class Scalajsld.Options ; was " + optimizerOptions.getClass())
            }
        }
        if (project.hasProperty(MODULE_INITIALIZERS)) {
            def moduleInitializers = project.property(MODULE_INITIALIZERS)
            if (moduleInitializers instanceof Seq<ModuleInitializer>) {
                options = options.withModuleInitializers(moduleInitializers)
            } else {
                project.error("ModuleInitializers not of class Seq<ModuleInitializer> ; was " + moduleInitializers.getClass())
            }
        }
        if (project.hasProperty(MIN_OUTPUT)) {
            destFile = project.file(project.property(MIN_OUTPUT))
        } else if (project.hasProperty(OUTPUT)) {
            destFile = project.file(project.property(OUTPUT))
        }
        options = options.withOutput(destFile)

        if (project.hasProperty(SEMANTICS)) {
            def semanticsObj = project.property(SEMANTICS)
            if (semanticsObj instanceof Semantics) {
                options = options.withSemantics(semanticsObj)
            } else {
                project.logger.error("Semantics not of type Semantics ; was " + semanticsObj.getClass())
            }

        } else if (fullOpt) {
            options = options.withSemantics(options.semantics().withProductionMode(true))
        }

        if (project.hasProperty(ES_FEATURES)) {
            def esFeatures = project.property(ES_FEATURES)
            //FIXME? Groovy doesn't see type ESFeatures
            if (esFeatures instanceof OutputMode) {
                options = options.withEsFeatures(esFeatures)
            } else {
                project.error("ESFeatures not of class OutputMode ; was " + esFeatures.getClass())
            }
        }

        if (project.hasProperty(MODULE_KIND)) {
            def moduleKind = project.property(MODULE_KIND)
            if (moduleKind instanceof ModuleKind) {
                options = options.withModuleKind(moduleKind)
            } else {
                project.error("ModuleKind not of class ModuleKind ; was " + moduleKind.getClass())
            }
        } else {
            options = options.withModuleKind(Utils.resolveModuleKind(project))
        }

        if (project.hasProperty(COMPLIANT)) {
            options = options.withCompliantsSemantics()
        }

        if (project.hasProperty(MIN_PRETTY) || project.hasProperty(PRETTY)) {
            options = options.withPrettyPrint(true)
        }
        if (project.hasProperty(MIN_N_SOURCEMAP) || project.hasProperty(N_SOURCEMAP)) {
            options = options.withSourceMap(false)
        }

        if (project.hasProperty(MIN_RELSM)) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property(MIN_RELSM))))
        } else if (project.hasProperty(RELSM)) {
            options = options.withRelativizeSourceMap(Option.apply(new URI((String) project.property(RELSM))))
        }

        if (project.hasProperty(BATCH)) {
            options = options.withBatchMode(true)
        }
        if (project.hasProperty(NO_PARALLEL)) {
            options = options.withParallel(false)
        }
        if (project.hasProperty(MIN_CHECKIR) || project.hasProperty(CHECKIR)) {
            options = options.withCheckIR(true)
        }
        if (project.hasProperty(STDLIB)) {
            def stdlib = project.property(STDLIB)
            if (stdlib instanceof String) {
                options = options.withStdLib(Option<File>.apply(new File(stdlib)))
            } else if (stdlib instanceof File) {
                options = options.withStdLib(Option<File>.apply(stdlib))
            } else {
                project.error("Stdlib not of class String or File ; was " + stdlib.getClass())
            }
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

        if (fullOpt) {
            options = options.withUseClosureCompiler(true)
        } else if (noOpt) {
            options = options.withDisableOptimizer(true)
        } else {
            options = options.withDisableOptimizer(false)
        }

        return options
    }

}
