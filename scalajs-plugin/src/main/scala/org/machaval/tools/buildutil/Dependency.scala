package org.machaval.tools.buildutil

import org.gradle.api.artifacts.Configuration

import java.io.File
import scala.collection.JavaConverters._

trait Dependency extends DependencyCoordinates {
  def artifactName: String

  def withVersion(version: Version): Dependency.WithVersion =
    new Dependency.WithVersion(dependency = this, version)
}

object Dependency {
  trait Findable extends DependencyCoordinates {
    def dependencyForArtifactName(artifactName: String): Option[Dependency]

    def findInConfiguration(configuration: Configuration): Option[Dependency.WithVersion] = {
      find(configuration.getDependencies.asScala.flatMap(DependencyData.fromGradleDependency))
    }

    def findInClassPath(classPath: Iterable[File]): Option[Dependency.WithVersion] =
      find(classPath.flatMap(DependencyData.fromFile))

    private def find(iterable: Iterable[DependencyData]): Option[Dependency.WithVersion] = {
      iterable.flatMap(d => {
        find(d)
      }).headOption
    }

    private def find(dependencyData: DependencyData): Option[Dependency.WithVersion] = {
      val version: Version = dependencyData.version
      val groupMatches: Boolean = dependencyData.group.isEmpty || dependencyData.group.contains(group)
      val classifierMatches: Boolean = this.classifier(version) == dependencyData.classifier
      val extensionMatches: Boolean =
        (this.extension(version) == dependencyData.extension) ||
          (this.extension(version).isEmpty && dependencyData.extension.contains("jar"))

      if (!groupMatches || !classifierMatches || !extensionMatches) {
        None
      } else {
        dependencyForArtifactName(dependencyData.artifactName).map(_.withVersion(version))
      }
    }
  }

  abstract class Simple(override val group: String,
                        override val artifact: String)
    extends Findable with Dependency {

    override def dependencyForArtifactName(artifactName: String): Option[Dependency] = {
      if (artifactName == artifact) {
        Some(this)
      } else {
        None
      }
    }

    override def artifactName: String = {
      artifact
    }
  }

  class WithVersion(val dependency: Dependency, override val version: Version) extends DependencyData {
    override def toString: String = s"'$dependencyNotation'"
    override def group: Option[String] = Some(dependency.group)
    override def artifactName: String = dependency.artifactName
    override def classifier: Option[String] = dependency.classifier(version)
    override def extension: Option[String] = dependency.extension(version)
  }
}
