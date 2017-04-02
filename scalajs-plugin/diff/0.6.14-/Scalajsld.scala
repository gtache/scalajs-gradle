package com.github.gtache

import java.io.File
import java.net.URI

import org.scalajs.core.tools.io._
import org.scalajs.core.tools.linker.Linker
import org.scalajs.core.tools.linker.backend.{LinkerBackend, OutputMode}
import org.scalajs.core.tools.linker.frontend.LinkerFrontend
import org.scalajs.core.tools.logging._
import org.scalajs.core.tools.sem._

/**
  * The object used to link and create the js file
  * (see https://github.com/scala-js/scala-js/blob/master/cli/src/main/scala/org/scalajs/cli/Scalajsld.scala)
  */
object Scalajsld {

  private var options: Options = Options()
  private var optionsChanged: Boolean = false

  //Store linker and cache to gain time
  private var linker: Linker = _
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
      val semantics: Semantics =
        if (options.fullOpt) {options.semantics.optimized}
        else {options.semantics}

      val frontendConfig = LinkerFrontend.Config()
        .withCheckIR(options.checkIR)

      val backendConfig = LinkerBackend.Config()
        .withRelativizeSourceMapBase(options.relativizeSourceMap)
        .withPrettyPrint(options.prettyPrint)

      linker = Linker(semantics, options.outputMode, options.sourceMap,
        disableOptimizer = options.noOpt, parallel = true,
        useClosureCompiler = options.fullOpt,
        frontendConfig, backendConfig)

      if (cache == null) {
        cache = (new IRFileCache).newCache
      }
      optionsChanged = false
    }
    //Reset linker if there is an error (else trying to link with the same one will throw an exception)
    try {
      linker.link(cache.cached(irContainers), outFile, logger)
    } catch {
      case e: Exception => linker = null
        throw e
    }
  }

  /**
    * A subclass containing the options for the linker
    *
    * @param cp                  the classpath
    * @param output              the output file
    * @param jsoutput            Deprecated
    * @param semantics           the semantics to be used
    * @param outputMode          the output mode
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
                     output: File = null,
                     jsoutput: Boolean = false,
                     semantics: Semantics = Semantics.Defaults,
                     outputMode: OutputMode = OutputMode.ECMAScript51Isolated,
                     noOpt: Boolean = false,
                     fullOpt: Boolean = false,
                     prettyPrint: Boolean = false,
                     sourceMap: Boolean = true,
                     relativizeSourceMap: Option[URI] = None,
                     bypassLinkingErrors: Boolean = false,
                     checkIR: Boolean = false,
                     stdLib: Option[File] = None,
                     logLevel: Level = Level.Info) {

    def withClasspath(newCp: Seq[File]): Options = {
      this.copy(cp = newCp)
    }

    def withOutput(newOutput: File): Options = {
      this.copy(output = newOutput)
    }

    def withJsOutput(newJsOutput: Boolean): Options = {
      this.copy(jsoutput = newJsOutput)
    }

    def withSemantics(newSemantics: Semantics): Options = {
      this.copy(semantics = newSemantics)
    }

    def withOutputMode(newOutputMode: OutputMode): Options = {
      this.copy(outputMode = newOutputMode)
    }

    def withNoOpt(): Options = {
      this.copy(fullOpt = false, noOpt = true)
    }

    def withFastOpt(): Options = {
      this.copy(fullOpt = false, noOpt = false)
    }

    def withFullOpt(): Options = {
      this.copy(fullOpt = true, noOpt = false)
    }

    def withPrettyPrint(newPrettyPrint: Boolean): Options = {
      this.copy(prettyPrint = newPrettyPrint)
    }

    def withSourceMap(newSourceMap: Boolean): Options = {
      this.copy(sourceMap = newSourceMap)
    }

    def withRelativizeSourceMap(newRelativizeSourceMap: Option[URI]): Options = {
      this.copy(relativizeSourceMap = newRelativizeSourceMap)
    }

    def withBypassLinkingErrors(newBypass: Boolean): Options = {
      this.copy(bypassLinkingErrors = newBypass)
    }

    def withCheckIR(newCheckIR: Boolean): Options = {
      this.copy(checkIR = newCheckIR)
    }

    def withStdLib(newStdLib: Option[File]): Options = {
      this.copy(stdLib = newStdLib)
    }

    def withLogLevel(newLogLevel: Level): Options = {
      this.copy(logLevel = newLogLevel)
    }

    def withCompliantsSemantics(): Options = {
      this.copy(semantics = semantics.withAsInstanceOfs(CheckedBehavior.Compliant))
    }


    override def toString: String = {
      "cp : " + cp.foldLeft("")((acc: String, c: File) => acc + "\n" + c.getAbsolutePath) + "\n" +
        "output : " + output + " jsoutput : " + jsoutput + "\n" +
        "semantics : " + semantics + "\n" +
        "outputMode : " + outputMode + "\n" +
        "NoOpt : " + noOpt + " ; fullOpt : " + fullOpt + "\n" +
        "prettyPrint : " + prettyPrint + "\n" +
        "sourcemap : " + sourceMap + "\n" +
        "relativizeSourceMap + " + relativizeSourceMap.getOrElse("No sourcemap") + "\n" +
        "bypass : " + bypassLinkingErrors + "\n" +
        "checkIR : " + checkIR + "\n" +
        "stdLib : " + stdLib + "\n" +
        "logLevel : " + logLevel
    }

    override def equals(that: Any): Boolean = {
      that match {
        case that: Options => {
          this.cp == that.cp &&
            this.output == that.output &&
            this.jsoutput == that.jsoutput &&
            this.semantics == that.semantics &&
            this.outputMode == that.outputMode &&
            this.noOpt == that.noOpt &&
            this.fullOpt == that.fullOpt &&
            this.prettyPrint == that.prettyPrint &&
            this.sourceMap == that.sourceMap &&
            this.relativizeSourceMap == that.relativizeSourceMap &&
            this.bypassLinkingErrors == that.bypassLinkingErrors &&
            this.checkIR == that.checkIR &&
            this.stdLib == that.stdLib &&
            this.logLevel == that.logLevel
        }
        case _ => false
      }
    }

    override def hashCode: Int = {
      cp.hashCode +
        2 * output.hashCode +
        3 * jsoutput.hashCode +
        5 * semantics.hashCode +
        7 * outputMode.hashCode +
        11 * noOpt.hashCode +
        13 * fullOpt.hashCode +
        17 * prettyPrint.hashCode +
        19 * sourceMap.hashCode +
        23 * relativizeSourceMap.hashCode +
        29 * bypassLinkingErrors.hashCode +
        31 * checkIR.hashCode +
        37 * stdLib.hashCode +
        41 * logLevel.hashCode
    }

  }

}
