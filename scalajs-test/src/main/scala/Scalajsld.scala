
import java.io.File
import java.net.URI

import org.scalajs.core.ir.ScalaJSVersions

import org.scalajs.core.tools.sem._
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.logging._

import CheckedBehavior.Compliant

import org.scalajs.core.tools.linker.Linker
import org.scalajs.core.tools.linker.frontend.LinkerFrontend
import org.scalajs.core.tools.linker.backend.{LinkerBackend, OutputMode}


object Scalajsld {
  def main(args : Array[String]): Unit ={
    assert(args.length==3)
    val src = args(0)
    val out = args(1)
    val std = args(2)

    case class Options(
                        cp: Seq[File] = Seq.empty,
                        output: File = null,
                        jsoutput: Boolean = false,
                        semantics: Semantics = Semantics.Defaults,
                        outputMode: OutputMode = OutputMode.ECMAScript51Isolated,
                        noOpt: Boolean = false,
                        fullOpt: Boolean = false,
                        prettyPrint: Boolean = false,
                        sourceMap: Boolean = false,
                        relativizeSourceMap: Option[URI] = None,
                        bypassLinkingErrors: Boolean = false,
                        checkIR: Boolean = false,
                        stdLib: Option[File] = None,
                        logLevel: Level = Level.Info)

    val options : Options = Options(cp=new File(src).listFiles(),output=new File(out))
    val classpath = options.stdLib.toList ++ options.cp
    val irContainers  = IRFileCache.IRContainer.fromClasspath(classpath)
    val semantics : Semantics =
      if (options.fullOpt) options.semantics.optimized
      else options.semantics

    val frontendConfig = LinkerFrontend.Config()
      .withCheckIR(options.checkIR)

    val backendConfig = LinkerBackend.Config()
      .withRelativizeSourceMapBase(options.relativizeSourceMap)
      .withPrettyPrint(options.prettyPrint)

    val linker = Linker(semantics, options.outputMode, options.sourceMap,
      disableOptimizer = options.noOpt, parallel = true,
      useClosureCompiler = options.fullOpt,
      frontendConfig, backendConfig)

    val logger = new ScalaConsoleLogger(options.logLevel)
    val outFile = WritableFileVirtualJSFile(options.output)
    val cache = (new IRFileCache).newCache

    linker.link(cache.cached(irContainers), outFile, logger)
  }

}
