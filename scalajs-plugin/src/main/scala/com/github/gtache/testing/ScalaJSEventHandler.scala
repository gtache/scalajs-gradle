package com.github.gtache.testing

import sbt.testing.{Event, EventHandler, TaskDef}

final class ScalaJSEventHandler(testStatus: ScalaJSTestStatus) extends EventHandler {
  override def handle(event: Event): Unit = {
    val fingerprint = event.fingerprint()
    val name = event.fullyQualifiedName()
    val status = event.status()
    val selector = event.selector()
    val taskDef = new TaskDef(name, fingerprint, false, Array(selector))
    status.name() match {
      case "Success" => testStatus.succeeded = testStatus.succeeded :+ taskDef
      case "Error" => testStatus.errored = testStatus.errored :+ taskDef
      case "Failure" => testStatus.failed = testStatus.failed :+ taskDef
      case "Skipped" => testStatus.skipped = testStatus.skipped :+ taskDef
      case "Ignored" => testStatus.ignored = testStatus.ignored :+ taskDef
      case "Canceled" => testStatus.canceled = testStatus.canceled :+ taskDef
      case "Pending" => testStatus.pending = testStatus.pending :+ taskDef
      case s: String => throw new IllegalStateException("Unknown task status : " + s)
    }
    val totLength = testStatus.succeeded.length +
      testStatus.failed.length +
      testStatus.errored.length

    if (testStatus.all.length == totLength) {
      testStatus.testingFinished()
    }

    println("\n" + testStatus + "\n")
  }
}