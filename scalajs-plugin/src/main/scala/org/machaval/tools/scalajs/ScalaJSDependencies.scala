package org.machaval.tools.scalajs

import org.gradle.api.artifacts.Configuration
import org.machaval.tools.build
import org.machaval.tools.build.Configurations
import org.machaval.tools.build.DependencyRequirement
import org.machaval.tools.build.ScalaDependency
import org.machaval.tools.build.ScalaLibrary
import org.machaval.tools.build.Version

import scala.collection.immutable.Seq

object ScalaJSDependencies {
  val configurationName: String = "scalajs"

  private val scalaJS: Configurations = Configurations.forName(configurationName)

  private val group: String = "org.scala-js"
  private val versionDefault: String = "1.9.0"
  private object Library extends ScalaDependency.Scala2(group, "scalajs-library")

  private object Linker extends ScalaDependency.Scala2(group, "scalajs-linker")

  private object Compiler extends ScalaDependency.Scala2(group, "scalajs-compiler", isScalaVersionFull = true)

  def dependencyRequirements(pluginScalaLibrary: ScalaLibrary,
                             projectScalaLibrary: ScalaLibrary,
                             implementationConfiguration: Configuration
                            ): Seq[DependencyRequirement] = {


    val scalaJSVersion: String = Library.findInConfiguration(implementationConfiguration)
      .map(_.version.version)
      .getOrElse(versionDefault)

    val forPluginClassPath: Seq[DependencyRequirement] =
      Seq(
        new build.ScalaDependency.Requirement(
          findable = Linker,
          version = new Version(scalaJSVersion),
          scalaLibrary = pluginScalaLibrary,
          reason = "because it is needed for linking the ScalaJS code",
          configurations = scalaJS
        )
      )

    val forProjectClassPath: Seq[DependencyRequirement] = Seq(
      new build.ScalaDependency.Requirement(
        findable = Compiler,
        version = new Version(scalaJSVersion),
        scalaLibrary = projectScalaLibrary,
        reason = "because it is needed for compiling of the ScalaJS code on Scala 2",
        configurations = Configurations.scalaCompilerPlugins
      ),

      new build.ScalaDependency.Requirement(
        findable = Library,
        version = new Version(scalaJSVersion),
        scalaLibrary = projectScalaLibrary,
        reason = "because it is needed for compiling of the ScalaJS code",
        configurations = Configurations.implementation
      )
    )

    forPluginClassPath ++ forProjectClassPath
  }
}
