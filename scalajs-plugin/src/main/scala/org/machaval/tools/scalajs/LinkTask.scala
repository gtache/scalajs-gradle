package org.machaval.tools.scalajs

import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.machaval.tools.buildutil.Gradle
import org.machaval.tools.util.Files

import java.io.File
import scala.collection.JavaConverters._

abstract class LinkTask extends DefaultTask with ScalaJSTask {

  setGroup("build")

  override protected def flavour: String = "Link"

  @TaskAction final def execute(): Unit = {
    // Needed to access ScalaJS linking functionality in Link.
    // Dynamically-loaded classes are can only be loaded after they are added to the classpath,
    // or Gradle decorating code breaks at the plugin load time for the Task subclasses.
    // So, dynamically-loaded classes are mentioned indirectly, only in the ScalaJS class.
    // It seems that expanding the classpath once, here, is enough for everything to work.

    Gradle.addToClassPath(this, Gradle.getConfiguration(getProject, ScalaJSDependencies.configurationName).asScala)
    ScalaJS(task = this, linkTask = this).link()
  }
  protected def sourceSetName: String = SourceSet.MAIN_SOURCE_SET_NAME

  private def sourceSet: SourceSet = Gradle.getSourceSet(getProject, sourceSetName)

  @Nested
  def getModuleInitializers: NamedDomainObjectContainer[ModuleInitializerProperties]
  def moduleInitializerProperties: Option[Seq[ModuleInitializerProperties]] = Some(getModuleInitializers.asScala.toSeq)

  @Classpath
  final def getRuntimeClassPath: FileCollection = sourceSet.getRuntimeClasspath

  @OutputFile
  final def getReportTextFile: File = outputFile("linking-report.txt")

  @OutputFile
  final def getReportBinFile: File = outputFile("linking-report.bin")

  def outputFile(name: String): File = Files.file(getProject.getBuildDir, "scalajs", getName, name)

  def optimization: Optimization = Gradle.byName(getOptimization, Optimization.Fast, Optimization.values().toList)

  @Input
  @Optional
  def getOptimization: Property[String]

  @Input
  @Optional
  def getModuleKind: Property[String]

  @Input
  @Optional
  def getModuleSplitStyle: Property[String]

  @Input
  @Optional
  def getPrettyPrint: Property[Boolean]

  @Input
  @Optional
  def getSourceMap: Property[Boolean]

  @Input
  @Optional
  def getOptimizer: Property[Boolean]

  @Input
  @Optional
  def getRelativizeSourceMap: Property[String]

  @Input
  @Optional
  def getJsDirectory: Property[String]

  @Input
  @Optional
  def getEsVersion: Property[String]

  getProject.afterEvaluate((project: Project) => {
    getDependsOn.add(Gradle.getClassesTask(project, sourceSet))
    setDescription(s"$flavour ScalaJS${optimization.description}")
  })
}

