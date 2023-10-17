package org.machaval.tools.buildutil

import org.machaval.tools.util.Files
import org.machaval.tools.util.Strings

import java.io.File

trait DependencyData {
  def group: Option[String]
  def artifactName: String
  def version: Version
  def classifier: Option[String]
  def extension: Option[String]

  def dependencyNotation: String = {
    val classifierStr: String = Strings.prefix(":", classifier)
    val extensionStr: String = Strings.prefix("@", extension)
    s"${group.get}:$artifactName:${version.version}$classifierStr$extensionStr"
  }

  def fileName: String = {
    val classifierStr: String = Strings.prefix("-", classifier)
    val extensionStr: String = extension.getOrElse("jar")
    s"$artifactName-${version.version}$classifierStr.$extensionStr"
  }
}

object DependencyData {
  def fromGradleDependency(dependency: org.gradle.api.artifacts.Dependency): Option[DependencyData] = {
    dependency match {
      case dependency: org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency =>
        Some(new DependencyData {
          override def group: Option[String] = Some(dependency.getGroup)
          override def artifactName: String = dependency.getName
          override def version: Version = Version(dependency.getVersion)
          override def classifier: Option[String] = None
          override def extension: Option[String] = Some("jar")
        })
      case _: org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency => None
      case _ => None
    }
  }

  def fromFile(file: File): Option[DependencyData] = {
    val (nameAndVersion: String, fileExtension: Option[String]) = Files.nameAndExtension(file.getName)
    val (name: String, versionOpt: Option[String]) = Strings.split(nameAndVersion, '-')
    if (versionOpt.isEmpty) {
      None
    } else {
      Some(new DependencyData {
        override def group: Option[String] = None
        override def artifactName: String = name
        override def version: Version = Version(versionOpt.get)
        override def classifier: Option[String] = None
        override def extension: Option[String] = fileExtension
      })
    }
  }
}
