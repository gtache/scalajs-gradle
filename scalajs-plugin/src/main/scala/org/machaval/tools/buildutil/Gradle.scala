package org.machaval.tools.buildutil

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.internal.classloader.ClassLoaderVisitor
import org.gradle.internal.classloader.ClasspathUtil
import org.gradle.internal.classloader.VisitableURLClassLoader
import org.machaval.tools.util.Files
import org.scalajs.linker.interface.ModuleKind

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import scala.collection.JavaConverters._
import scala.collection.mutable

object Gradle {
  def byName[T](property: Property[String], default: => T, all: List[T]): T = {
    if (!property.isPresent) {
      default
    } else {
      all.find(_.toString == property.get).get
    }
  }

  def moduleKind(property: Property[String]): ModuleKind = {
    byName(property, ModuleKind.NoModule, ModuleKind.All)
  }

  def getSourceSet(project: Project, sourceSetName: String): SourceSet = {
    val sourceSet = project.getExtensions.getByType(classOf[JavaPluginExtension])
      .getSourceSets.getByName(sourceSetName)
    sourceSet
  }

  def getClassesTask(project: Project, sourceSet: SourceSet): Task = {
    project.getTasks.getByName(sourceSet.getClassesTaskName)
  }

  def getScalaCompile(project: Project, sourceSet: SourceSet): ScalaCompile = {
    val task = getClassesTask(project, sourceSet)
    task
      .getDependsOn
      .asScala
      .find(classOf[TaskProvider[ScalaCompile]].isInstance)
      .get
      .asInstanceOf[TaskProvider[ScalaCompile]]
      .get
  }

  def getConfiguration(project: Project, name: String): Configuration = {
    project.getConfigurations.getByName(name)
  }

  def addToClassPath(obj: AnyRef, files: Iterable[File]): ClassLoader = {
    val result: ClassLoader = obj.getClass.getClassLoader
    val urls: Iterable[URL] = files.map(_.toURI.toURL)
    result match {
      case visitable: VisitableURLClassLoader =>
        for (url <- urls ) {
          visitable.addURL(url)
        }
      case classLoader =>
        ClasspathUtil.addUrl(
          classLoader.asInstanceOf[URLClassLoader],
          urls.asJava
        )
    }
    result
  }

  def collectClassPath(classLoader: ClassLoader): Seq[File] = {
    val result: mutable.ArrayBuffer[File] = mutable.ArrayBuffer.empty[File]

    val visitor: ClassLoaderVisitor = new ClassLoaderVisitor() {
      override def visitClassPath(classPath: Array[URL]): Unit = {
        for (url <- classPath if url.getProtocol == "file") {
          result += Files.url2file(url)
        }
      }
    }
    visitor.visit(classLoader)
    result.toSeq
  }
}
