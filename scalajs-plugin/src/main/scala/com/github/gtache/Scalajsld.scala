package com.github.gtache

import java.io.File
import java.net.URI

import org.scalajs.core.tools.io._
import org.scalajs.core.tools.linker.Linker
import org.scalajs.core.tools.linker.backend.{LinkerBackend, OutputMode}
import org.scalajs.core.tools.linker.frontend.LinkerFrontend
import org.scalajs.core.tools.logging._
import org.scalajs.core.tools.sem.CheckedBehavior.Compliant
import org.scalajs.core.tools.sem._

/**
  * The object used to link and create the js file
  * (see https://github.com/scala-js/scala-js/blob/master/cli/src/main/scala/org/scalajs/cli/Scalajsld.scala)
  */
object Scalajsld {

  var options: Options = new Options()
  private var optionsChanged: Boolean = false

  //Store linker and cache to gain time
  private var linker: Linker = null
  private var cache: IRFileCache#Cache = null

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
    new Options()
  }

  /**
    * Executes the linker
    */
  def exec(): Unit = {
    val classpath = options.stdLib.toList ++ options.cp
    val irContainers = IRFileCache.IRContainer.fromClasspath(classpath)
    val logger = new ScalaConsoleLogger(options.logLevel)
    val outFile = WritableFileVirtualJSFile(options.output)
    if (optionsChanged || linker == null || cache == null) {
      val semantics: Semantics =
        if (options.fullOpt) options.semantics.optimized
        else options.semantics

      val frontendConfig = LinkerFrontend.Config()
        .withCheckIR(options.checkIR)

      val backendConfig = LinkerBackend.Config()
        .withRelativizeSourceMapBase(options.relativizeSourceMap)
        .withPrettyPrint(options.prettyPrint)

      linker = Linker(semantics, options.outputMode, options.sourceMap,
        disableOptimizer = options.noOpt, parallel = true,
        useClosureCompiler = options.fullOpt,
        frontendConfig, backendConfig)

      cache = (new IRFileCache).newCache

      optionsChanged = false
    }
    linker.link(cache.cached(irContainers), outFile, logger)
  }

  /**
    * A subclass containing the options for the linker
    *
    * @param cp                  the classpath
    * @param output              the output file
    * @param jsoutput            Deprecated
    * @param semantics
    * @param outputMode          the output mode
    * @param noOpt               with no optimization
    * @param fullOpt             with full optimization
    * @param prettyPrint         with pretty print
    * @param sourceMap
    * @param relativizeSourceMap
    * @param bypassLinkingErrors bypass errors or not
    * @param checkIR
    * @param stdLib              a library to be used for the linking
    * @param logLevel            the level of the logging to be displayed
    */
  class Options(val cp: Seq[File] = Seq.empty,
                val output: File = null,
                val jsoutput: Boolean = false,
                val semantics: Semantics = Semantics.Defaults,
                val outputMode: OutputMode = OutputMode.ECMAScript51Isolated,
                val noOpt: Boolean = false,
                val fullOpt: Boolean = false,
                val prettyPrint: Boolean = false,
                val sourceMap: Boolean = false,
                val relativizeSourceMap: Option[URI] = None,
                val bypassLinkingErrors: Boolean = false,
                val checkIR: Boolean = false,
                val stdLib: Option[File] = None,
                val logLevel: Level = Level.Info) {

    def withClasspath(newCp: Seq[File]): Options = {
      new Options(newCp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withOutput(newOutput: File): Options = {
      new Options(this.cp, newOutput, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withJsOutput(newJsOutput: Boolean): Options = {
      new Options(this.cp, this.output, newJsOutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withSemantics(newSemantics: Semantics): Options = {
      new Options(this.cp, this.output, this.jsoutput, newSemantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withOutputMode(newOutputMode: OutputMode): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, newOutputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withNoOpt(): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, true, false,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withFastOpt(): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, false, false,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withFullOpt(): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, false, true,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withPrettyPrint(newPrettyPrint: Boolean): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        newPrettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withSourceMap(newSourceMap: Boolean): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, newSourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withRelativizeSourceMap(newRelativizeSourceMap: Option[URI]): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, newRelativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withBypassLinkingErrors(newBypass: Boolean): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, newBypass,
        this.checkIR, this.stdLib, this.logLevel)
    }

    def withCheckIR(newCheckIR: Boolean): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        newCheckIR, this.stdLib, this.logLevel)
    }

    def withStdLib(newStdLib: Option[File]): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, newStdLib, this.logLevel)
    }

    def withLogLevel(newLogLevel: Level): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics, this.outputMode, this.noOpt, this.fullOpt,
        this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, newLogLevel)
    }

    def withCompliantsSemantics(): Options = {
      new Options(this.cp, this.output, this.jsoutput, this.semantics.withAsInstanceOfs(Compliant), this.outputMode,
        this.noOpt, this.fullOpt, this.prettyPrint, this.sourceMap, this.relativizeSourceMap, this.bypassLinkingErrors,
        this.checkIR, this.stdLib, this.logLevel)
    }

    /**
      * Returns a string representation of this object
      *
      * @return a string
      */
    override def toString(): String = {
      "cp : " + cp + "\n" +
        "output : " + output + " jsoutput : " + jsoutput + "\n" +
        "semantics : " + semantics + "\n" +
        "outputMode : " + outputMode + "\n" +
        "NoOpt : " + noOpt + " ; fullOpt : " + fullOpt + "\n" +
        "prettyPrint : " + prettyPrint + "\n" +
        "sourcemap : " + sourceMap + " ; " + relativizeSourceMap + "\n" +
        "bypass : " + bypassLinkingErrors + "\n" +
        "checkIR : " + checkIR + "\n" +
        "stdLib : " + stdLib + "\n" +
        "logLevel : " + logLevel
    }

    /**
      * Checks if the options given in argument are the same as this instance
      *
      * @param that the options to compare
      * @return true if they are the same, false otherwise
      */
    def equals(that: Options): Boolean = {
      if (!that.getClass.equals(this.getClass)) {
        false
      } else {
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
    }

  }

}
