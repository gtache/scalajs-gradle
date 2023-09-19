package org.machaval.tools.scalajs

import org.gradle.api.Task

trait ScalaJSTask extends Task {
  
  setDescription(s"$flavour ScalaJS")

  protected def flavour: String
  
}
