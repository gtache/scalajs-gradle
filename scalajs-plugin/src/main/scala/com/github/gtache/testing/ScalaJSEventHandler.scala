package com.github.gtache.testing

import sbt.testing.{Event, EventHandler, TaskDef}

object ScalaJSEventHandler extends EventHandler {
  override def handle(event: Event): Unit = {
    val fingerprint = event.fingerprint()
    val name = event.fullyQualifiedName()
    val status = event.status()
    val selector = event.selector()
    val taskDef = new TaskDef(name, fingerprint, false, Array(selector))
    status.name() match {
      case "Success" => ScalaJSTestStatus.succeeded = ScalaJSTestStatus.succeeded :+ taskDef
      case "Error" => ScalaJSTestStatus.errored = ScalaJSTestStatus.errored :+ taskDef
      case "Failure" => ScalaJSTestStatus.failed = ScalaJSTestStatus.failed :+ taskDef
      case "Skipped" => ScalaJSTestStatus.skipped = ScalaJSTestStatus.skipped :+ taskDef
      case "Ignored" => ScalaJSTestStatus.ignored = ScalaJSTestStatus.ignored :+ taskDef
      case "Canceled" => ScalaJSTestStatus.canceled = ScalaJSTestStatus.canceled :+ taskDef
      case "Pending" => ScalaJSTestStatus.pending = ScalaJSTestStatus.pending :+ taskDef
      case s: String => throw new IllegalStateException("Unknown task status : " + s)
    }
    val totLength = ScalaJSTestStatus.succeeded.length +
      ScalaJSTestStatus.failed.length +
      ScalaJSTestStatus.errored.length
    if (ScalaJSTestStatus.all.length == totLength) {
      ScalaJSTestStatus.testingFinished()
    }
    println(ScalaJSTestStatus)
  }
}