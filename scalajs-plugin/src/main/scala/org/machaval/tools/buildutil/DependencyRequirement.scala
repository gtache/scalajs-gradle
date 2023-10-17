package org.machaval.tools.buildutil

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

import scala.collection.JavaConverters._

abstract class DependencyRequirement(findable: Dependency.Findable,
                                     version: Version,
                                     reason: String,
                                     configurations: Configurations,
                                     isVersionExact: Boolean = false) {

  // Note: all applyToConfiguration() must be run first: once a applyToClassPath() runs,
  // configuration is no longer changeable.
  private def applyToConfiguration(project: Project): Dependency.WithVersion = {
    val found: Option[Dependency.WithVersion] = findable
      .findInConfiguration(Gradle.getConfiguration(project, configurations.configuration))
    found.foreach(verify(_, project))
    found.getOrElse {
      val configuration: Configuration = getConfiguration(project)
      val toAdd: Dependency.WithVersion = getDependencyWithVersion
      project.getLogger.info(s"Adding dependency $toAdd to the $configuration $reason", null, null, null)
      configuration
        .getDependencies
        .add(project.getDependencies.create(toAdd.dependencyNotation))
      toAdd
    }
  }

  // Note: Once a classpath is resolved (e.g., by enumerating JARs on it :)),
  // dependencies can not be added to the configurations involved:
  //   Cannot change dependencies of dependency configuration ... after it has been included in dependency resolution
  // So the only thing we can do is to report the missing dependency:
  private def applyToClassPath(project: Project): Dependency.WithVersion = {
    val found: Option[Dependency.WithVersion] = findable.findInClassPath(
      Gradle.getConfiguration(project, configurations.classPath).asScala
    )
    found.foreach(verify(_, project))
    found.getOrElse({
      val configuration: Configuration = getConfiguration(project)
      val toAdd: Dependency.WithVersion = getDependencyWithVersion
      throw new GradleException(s"Please add dependency $toAdd to the $configuration $reason")
    })
  }

  protected def verify(found: Dependency.WithVersion, project: Project): Unit = {
    if (isVersionExact && found.version != version) {
      project.getLogger.info(s"Found $found, but the project uses version $version", null, null, null)
    }
  }

  private def getConfiguration(project: Project): Configuration = {
    Gradle.getConfiguration(project, configurations.configuration)
  }

  private def getDependencyWithVersion: Dependency.WithVersion = {
    getDependency.withVersion(version)
  }

  protected def getDependency: Dependency

}

object DependencyRequirement {

  def applyToProject(requirements: Seq[DependencyRequirement], project: Project): Unit = {
    requirements.foreach(_.applyToConfiguration(project))
    requirements.foreach(_.applyToClassPath(project))
  }
}
