package org.machaval.tools.buildutil

import org.gradle.api.artifacts.Configuration

import java.io.File

class ScalaLibrary(val scala2: Option[Dependency.WithVersion]) {

  require(scala2.nonEmpty, "No Scala library!")

  override def toString: String = s"ScalaLibrary(scala2=${scala2.map(_.version)})"

  def verify(other: ScalaLibrary): Unit = {
    val (actualMajor, actualMinor, _) = other.scala2.get.version.majorMinorMicro
    val (major, minor, _) = scala2.get.version.majorMinorMicro

    require(actualMajor == major && actualMinor == minor,
        s"Invalid scala 2 version should be $actualMajor.$actualMinor.x but was ${other.scala2.get.version}")
  }
}

object ScalaLibrary {

  val group: String = "org.scala-lang"

  abstract class Scala(artifact: String) extends JavaDependency(group = group, artifact) {

    def versionMajor: Int

    def getScalaVersion(library: ScalaLibrary): Version
  }

  object Scala2 extends Scala("scala-library") {
    override def versionMajor: Int = 2

    override def getScalaVersion(library: ScalaLibrary): Version = {
      library.scala2.get.version
    }
  }

  def getFromConfiguration(configuration: Configuration): ScalaLibrary = {
    new ScalaLibrary(
      scala2 = Scala2.findInConfiguration(configuration)
    )
  }

  def getFromClasspath(classPath: Iterable[File]): ScalaLibrary = {
    val result: ScalaLibrary = new ScalaLibrary(
      scala2 = Scala2.findInClassPath(classPath)
    )
    require(result.scala2.nonEmpty, "No Scala 2 library!")
    result
  }
}
