package com.github.gtache

import java.io.File
import java.net.URI

import org.scalajs.core.tools.io._
import org.scalajs.core.tools.linker.backend.ModuleKind
import org.scalajs.core.tools.linker.{CheckedBehavior => _, Semantics => _, _}
import org.scalajs.core.tools.logging._
import org.scalajs.core.tools.sem._

/**
  * The object used to link and create the js file
  * (see https://github.com/scala-js/scala-js/blob/master/cli/src/main/scala/org/scalajs/cli/Scalajsld.scala)
  */
object Scalajsld {

  //Linker options
  val MODULE_INITIALIZERS: String = "moduleInitializers"
  val MIN_OUTPUT: String = "o"
  val OUTPUT: String = "output"
  val SEMANTICS: String = "semantics"
  val ES_FEATURES: String = "esFeatures"
  val MODULE_KIND: String = "moduleKind"
  val COMPLIANT: String = "compliantAsInstanceOfs"
  val MIN_PRETTY: String = "p"
  val PRETTY: String = "prettyPrint"
  val MIN_N_SOURCEMAP: String = "noS"
  val N_SOURCEMAP: String = "noSourceMap"
  val MIN_RELSM: String = "r"
  val RELSM: String = "relativizeSourceMap"
  val BATCH: String = "batch"
  val NO_PARALLEL: String = "noParallel"
  val MIN_CHECKIR: String = "c"
  val CHECKIR: String = "checkIR"
  val STDLIB: String = "stdLib"
  val MIN_DEBUG: String = "d"
  val DEBUG: String = "debug"
  val MIN_WARN: String = "q"
  val WARN: String = "quiet"
  val MIN_ERR: String = "qq"
  val ERR: String = "really-quiet"
  val LOG_LEVEL: String = "linkLogLevel"

  val OPTIONS: String = "oOptions"

  private var options: Options = Options()
  private var optionsChanged: Boolean = false

  //Store linker and cache to gain time
  private var linker: ClearableLinker = _
  private var cache: IRFileCache#Cache = _

  /**
    * Returns the current options for the linker
    *
    * @return the options
    */
  def getOptions: Options = options

  /**
    * Changes the option of the linker
    *
    * @param newOptions the new options to be set
    */
  def setOptions(newOptions: Options): Unit = {
    this.options = newOptions
    optionsChanged = true
  }

  /**
    * Returns the default options
    *
    * @return the default options
    */
  def defaultOptions(): Options = {
    Options()
  }

  /**
    * Executes the linker
    */
  def exec(): Unit = {
    val classpath = options.stdLib.toList ++ options.cp
    val irContainers = IRFileCache.IRContainer.fromClasspath(classpath)
    val logger = new ScalaConsoleLogger(options.logLevel)
    val outFile = WritableFileVirtualJSFile(options.output)
    if (optionsChanged || linker == null) {

      val config = StandardLinker.Config()
        .withBatchMode(options.batchMode)
        .withCheckIR(options.checkIR)
        .withClosureCompilerIfAvailable(options.fullOpt)
        .withESFeatures(options.esFeatures)
        .withModuleKind(options.moduleKind)
        .withOptimizer(!options.noOpt)
        .withParallel(options.parallel)
        .withPrettyPrint(options.prettyPrint)
        .withRelativizeSourceMapBase(options.relativizeSourceMap)
        .withSemantics(if (options.fullOpt) options.semantics.optimized else options.semantics)
        .withSourceMap(options.sourceMap)
      linker = new ClearableLinker(() => StandardLinker(config), options.batchMode)
      if (cache == null) {
        cache = (new IRFileCache).newCache
      }
      try {
        linker.link(cache.cached(irContainers), options.moduleInitializers, outFile, logger)
      } catch {
        case e: Exception => linker = null
          throw e
      }
    }

    optionsChanged = false


  }

  /**
    * A subclass containing the options for the linker
    *
    * @param cp                  the classpath
    * @param moduleInitializers  the initializers
    * @param output              the output file
    * @param semantics           the semantics to be used
    * @param moduleKind          the type of the modules
    * @param noOpt               with no optimization
    * @param fullOpt             with full optimization
    * @param prettyPrint         with pretty print
    * @param sourceMap           if the sourcemap has to be emitted
    * @param relativizeSourceMap a sourcemap to use for linking
    * @param bypassLinkingErrors bypass errors or not (deprecated)
    * @param checkIR             checks the sjsir (expensive)
    * @param stdLib              a library to be used for the linking
    * @param logLevel            the level of the logging to be displayed
    */

  case class Options(cp: Seq[File] = Seq.empty,
                     moduleInitializers: Seq[ModuleInitializer] = Seq.empty,
                     output: File = null,
                     semantics: Semantics = Semantics.Defaults,
                     esFeatures: ESFeatures = ESFeatures.Defaults,
                     moduleKind: ModuleKind = ModuleKind.NoModule,
                     noOpt: Boolean = false,
                     fullOpt: Boolean = false,
                     prettyPrint: Boolean = false,
                     sourceMap: Boolean = true,
                     relativizeSourceMap: Option[URI] = None,
                     bypassLinkingErrors: Boolean = false,
                     batchMode: Boolean = false,
                     parallel: Boolean = true,
                     checkIR: Boolean = false,
                     stdLib: Option[File] = None,
                     logLevel: Level = Level.Info) {

    def withCompliantsSemantics(): Options = {
      this.copy(semantics = semantics.withAsInstanceOfs(CheckedBehavior.Compliant))
    }

    def withOptimizerOptions(newOptions: Options): Options = {
      this.copy(bypassLinkingErrors = newOptions.bypassLinkingErrors,
        parallel = newOptions.parallel,
        batchMode = newOptions.batchMode,
        noOpt = newOptions.noOpt,
        prettyPrint = newOptions.prettyPrint,
        checkIR = newOptions.checkIR,
        fullOpt = newOptions.fullOpt,
        cp = newOptions.cp,
        logLevel = newOptions.logLevel,
        moduleInitializers = newOptions.moduleInitializers,
        moduleKind = newOptions.moduleKind,
        output = newOptions.output,
        relativizeSourceMap = newOptions.relativizeSourceMap,
        sourceMap = newOptions.sourceMap,
        semantics = newOptions.semantics,
        stdLib = newOptions.stdLib,
        esFeatures = newOptions.esFeatures)
    }

    def withClasspath(newCp: Seq[File]): Options = {
      this.copy(cp = newCp)
    }

    def withModuleInitializers(newModuleIntializers: Seq[ModuleInitializer]): Options = {
      this.copy(moduleInitializers = newModuleIntializers)
    }

    def withOutput(newOutput: File): Options = {
      this.copy(output = newOutput)
    }

    def withSemantics(newSemantics: Semantics): Options = {
      this.copy(semantics = newSemantics)
    }

    def withModuleKind(newModuleKind: ModuleKind): Options = {
      this.copy(moduleKind = newModuleKind)
    }

    def withEsFeatures(esFeatures: ESFeatures): Options = {
      this.copy(esFeatures = esFeatures)
    }

    def withSourceMap(newSourceMap: Boolean): Options = {
      this.copy(sourceMap = newSourceMap)
    }

    def withRelativizeSourceMap(newRelativizeSourceMap: Option[URI]): Options = {
      this.copy(relativizeSourceMap = newRelativizeSourceMap)
    }

    def withStdLib(newStdLib: Option[File]): Options = {
      this.copy(stdLib = newStdLib)
    }

    def withLogLevel(newLogLevel: Level): Options = {
      this.copy(logLevel = newLogLevel)
    }

    def withDisableOptimizer(disable: Boolean): Options = {
      this.copy(noOpt = disable)
    }

    def withUseClosureCompiler(enabled: Boolean): Options = {
      this.copy(fullOpt = enabled)
    }

    def withPrettyPrint(newPrettyPrint: Boolean): Options = {
      this.copy(prettyPrint = newPrettyPrint)
    }

    def withBypassLinkingErrors(newBypass: Boolean): Options = {
      this.copy(bypassLinkingErrors = newBypass)
    }

    def withCheckIR(newCheckIR: Boolean): Options = {
      this.copy(checkIR = newCheckIR)
    }

    def withBatchMode(newBatchMode: Boolean): Options = {
      this.copy(batchMode = newBatchMode)
    }

    def withParallel(newParallel: Boolean): Options = {
      this.copy(parallel = newParallel)
    }

    override def toString: String = {
      "cp : " + cp.foldLeft("")((acc: String, c: File) => acc + "\n" + c.getAbsolutePath) + "\n" +
        "moduleInitializers : " + moduleInitializers.mkString(", ") + "\n" +
        "semantics : " + semantics + "\n" +
        "esFeatures : " + esFeatures + "\n" +
        "moduleKind : " + moduleKind + "\n" +
        "NoOpt : " + noOpt + " ; fullOpt : " + fullOpt + "\n" +
        "prettyPrint : " + prettyPrint + "\n" +
        "sourcemap : " + sourceMap + "\n" +
        "relativizeSourceMap + " + relativizeSourceMap.getOrElse("No sourcemap") + "\n" +
        "bypass : " + bypassLinkingErrors + "\n" +
        "parallel : " + parallel + "\n" +
        "batch mode : " + batchMode + "\n" +
        "checkIR : " + checkIR + "\n" +
        "stdLib : " + stdLib + "\n" +
        "logLevel : " + logLevel
    }

    override def equals(that: Any): Boolean = {
      that match {
        case that: Options =>
          this.cp == that.cp &&
            this.moduleInitializers == that.moduleInitializers &&
            this.output == that.output &&
            this.esFeatures == that.esFeatures &&
            this.semantics == that.semantics &&
            this.moduleKind == that.moduleKind &&
            this.noOpt == that.noOpt &&
            this.fullOpt == that.fullOpt &&
            this.prettyPrint == that.prettyPrint &&
            this.sourceMap == that.sourceMap &&
            this.relativizeSourceMap == that.relativizeSourceMap &&
            this.bypassLinkingErrors == that.bypassLinkingErrors &&
            this.parallel == that.parallel &&
            this.batchMode == that.batchMode &&
            this.checkIR == that.checkIR &&
            this.stdLib == that.stdLib &&
            this.logLevel == that.logLevel
        case _ => false
      }
    }

    override def hashCode: Int = {
      cp.hashCode +
        47 * moduleInitializers.hashCode +
        2 * output.hashCode +
        3 * esFeatures.hashCode +
        5 * semantics.hashCode +
        71 * moduleKind.hashCode +
        11 * noOpt.hashCode +
        13 * fullOpt.hashCode +
        17 * prettyPrint.hashCode +
        19 * sourceMap.hashCode +
        23 * relativizeSourceMap.hashCode +
        29 * bypassLinkingErrors.hashCode +
        97 * parallel.hashCode() +
        101 * batchMode.hashCode() +
        31 * checkIR.hashCode +
        37 * stdLib.hashCode +
        41 * logLevel.hashCode

    }

  }

}
