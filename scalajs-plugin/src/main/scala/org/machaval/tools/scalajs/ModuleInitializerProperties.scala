package org.machaval.tools.scalajs

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class ModuleInitializerProperties {
  @Input
  def getName: String // Type must have a read-only 'name' property

  @Input
  def getClassName: Property[String]

  @Input
  @Optional
  def getMainMethodName: Property[String]

  @Input
  @Optional
  def getMainMethodHasArgs: Property[Boolean]
}
