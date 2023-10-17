package org.machaval.tools.scalajs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.machaval.tools.buildutil.Configurations
import org.machaval.tools.buildutil.DependencyRequirement
import org.machaval.tools.buildutil.Gradle
import org.machaval.tools.buildutil.ScalaLibrary

import collection.JavaConverters._
import scala.collection.immutable.Seq

class ScalaJSPlugin extends Plugin[Project] {
  override def apply(project: Project): Unit = {
    project.getPluginManager.apply(classOf[org.gradle.api.plugins.scala.ScalaPlugin])

    val scalaJS: Configuration = project.getConfigurations.create(ScalaJSDependencies.configurationName)
    scalaJS.setVisible(false)
    scalaJS.setCanBeConsumed(false)
    scalaJS.setDescription("ScalaJS dependencies used by the ScalaJS plugin.")

    val linkMain: LinkTask = project.getTasks.create("link", classOf[LinkTask])

    project.afterEvaluate((project: Project) => {
      val implementationConfiguration: Configuration = Gradle.getConfiguration(project, Configurations.implementationConfiguration)
      val pluginScalaLibrary: ScalaLibrary = ScalaLibrary.getFromClasspath(Gradle.collectClassPath(getClass.getClassLoader))
      val projectScalaLibrary: ScalaLibrary = ScalaLibrary.getFromConfiguration(implementationConfiguration)

      val requirements: Seq[DependencyRequirement] = ScalaJSDependencies.dependencyRequirements(
        pluginScalaLibrary,
        projectScalaLibrary,
        implementationConfiguration
      )

      DependencyRequirement.applyToProject(requirements, project)

      projectScalaLibrary.verify(
        ScalaLibrary.getFromClasspath(Gradle.getConfiguration(project, Configurations.implementationClassPath).asScala)
      )
    })
  }
}
