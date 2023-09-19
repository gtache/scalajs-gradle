package org.machaval.tools.scalajs

import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.{LogLevel => GLevel}
import org.machaval.tools.build.Gradle
import org.machaval.tools.util.Files
import org.scalajs.linker.PathIRContainer
import org.scalajs.linker.PathOutputDirectory
import org.scalajs.linker.StandardImpl
import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.IRContainer
import org.scalajs.linker.interface.IRFile
import org.scalajs.linker.interface.LinkingException
import org.scalajs.linker.interface.ModuleInitializer
import org.scalajs.linker.interface.ModuleKind
import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.Semantics
import org.scalajs.linker.interface.StandardConfig
import org.scalajs.logging.{Level => JSLevel}

import java.io.File
import java.net.URI
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class ScalaJS(task: ScalaJSTask, linkTask: LinkTask) {

  private def logger: Logger = task.getLogger

  private lazy val jsDirectory: File =  {
    if (linkTask.getJsDirectory.isPresent) {
      linkTask.getProject.file(linkTask.getJsDirectory.get())
    } else {
      linkTask.outputFile("js")
    }
  }

  private def reportBinFile: File = linkTask.getReportBinFile

  private def moduleKind: ModuleKind = Gradle.byName(linkTask.getModuleKind, ModuleKind.NoModule, ModuleKind.All)

  private def jsLogger: org.scalajs.logging.Logger = new org.scalajs.logging.Logger {
    private def logSource: String = s"ScalaJS ${task.getName}"

    override def trace(t: => Throwable): Unit = {
      logger.error(s"$logSource Error", t)
    }

    override def log(level: JSLevel, message: => String): Unit = {
      logger.log(ScalaJS.scalajs2gradleLevel(level), s"$logSource: $message")
    }
  }

  def link(): Unit = {
    val reportTextFile: File = linkTask.getReportTextFile

    val moduleInitializers: Seq[ModuleInitializer] = linkTask
      .moduleInitializerProperties
      .map(_.map(ScalaJS.toModuleInitializer))
      .getOrElse(
        // Note: tests use fixed entry point
        Seq(ModuleInitializer.mainMethod(
          TestAdapterInitializer.ModuleClassName,
          TestAdapterInitializer.MainMethodName
        ))
      )

    // Note: if moved into the caller breaks class loading
    val linkerConfig: StandardConfig = {
      val fullOptimization: Boolean = linkTask.optimization == Optimization.Full
      var config = StandardConfig()
        .withCheckIR(fullOptimization)
        .withSemantics(if (fullOptimization) Semantics.Defaults.optimized else Semantics.Defaults)
        .withModuleKind(moduleKind)
        //.withClosureCompiler(fullOptimization && (moduleKind == ModuleKind.ESModule))
        .withClosureCompiler(fullOptimization)
        .withModuleSplitStyle(Gradle.byName(linkTask.getModuleSplitStyle, ModuleSplitStyle.FewestModules, ScalaJS.moduleSplitStyles))
        .withPrettyPrint(linkTask.getPrettyPrint.getOrElse(false))
        .withSourceMap(linkTask.getSourceMap.getOrElse(true))
        .withOptimizer(linkTask.getOptimizer.getOrElse(true))

      if (linkTask.getEsVersion.isPresent) {
        val esVersion = Gradle.byName(linkTask.getEsVersion, ESVersion.ES2018, ScalaJS.esVersions)
        config = config.withESFeatures(_.withESVersion(esVersion))
      }

      if (linkTask.getRelativizeSourceMap.isPresent) {
        config = config.withRelativizeSourceMapBase(Option(new URI(linkTask.getRelativizeSourceMap.get())))
      }
      config
    }

    logger.log(LogLevel.INFO,
      s"""ScalaJSPlugin ${task.getName}:
         |JSDirectory = $jsDirectory
         |reportFile = $reportTextFile
         |moduleInitializers = ${moduleInitializers.map(ModuleInitializer.fingerprint).mkString(", ")}
         |linkerConfig = $linkerConfig
         |""".stripMargin,
      null, null, null)

    jsDirectory.mkdirs()

    try {
      val report: Report = Await.result(atMost = Duration.Inf, awaitable = PathIRContainer
        .fromClasspath({
          val files: Seq[File] = linkTask.getRuntimeClassPath.getFiles.asScala.toSeq
          val paths = files.map(_.toPath)
          paths
        })
        .map(_._1)
        .flatMap((irContainers: Seq[IRContainer]) => {
          StandardImpl.irFileCache.newCache.cached(irContainers)
        })
        .flatMap((irFiles: Seq[IRFile]) => StandardImpl.linker(linkerConfig).link(
          irFiles = irFiles,
          moduleInitializers = moduleInitializers,
          output = PathOutputDirectory(jsDirectory.toPath),
          logger = jsLogger
        ))
      )
      Files.write(reportTextFile, report.toString)
      Files.writeBytes(reportBinFile, Report.serialize(report))
    } catch {
      case e: LinkingException => throw new GradleException("ScalaJS link error", e)
    }
  }
}

object ScalaJS {

  private val moduleSplitStyles: List[ModuleSplitStyle] = List(
    ModuleSplitStyle.FewestModules,
    ModuleSplitStyle.SmallestModules
  )

  private val esVersions: List[ESVersion] = List(
    ESVersion.ES5_1,
    ESVersion.ES2015,
    ESVersion.ES2016,
    ESVersion.ES2017,
    ESVersion.ES2018,
    ESVersion.ES2019,
    ESVersion.ES2020,
    ESVersion.ES2021,
  )

  private def toModuleInitializer(properties: ModuleInitializerProperties): ModuleInitializer = {
    val clazz: String = properties.getClassName.get
    val method: String = properties.getMainMethodName.getOrElse("main")
    val result: ModuleInitializer =
      if (properties.getMainMethodHasArgs.getOrElse(false)) {
        ModuleInitializer.mainMethodWithArgs(clazz, method)
      } else {
        ModuleInitializer.mainMethod(clazz, method)
      }
    result.withModuleID(properties.getName)
  }

  private def scalajs2gradleLevel(level: JSLevel): GLevel = {
    level match {
      case JSLevel.Error => GLevel.ERROR
      case JSLevel.Warn => GLevel.WARN
      case JSLevel.Info => GLevel.INFO
      case JSLevel.Debug => GLevel.DEBUG
    }
  }

  def apply(task: ScalaJSTask, linkTask: LinkTask): ScalaJS = new ScalaJS(task, linkTask)
}
