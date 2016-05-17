package com.github.gtache.testing

import sbt.testing.{Event, EventHandler, TaskDef}

/**
  * Handles events fired by a Framework
  *
  * @param testStatus The ScalaJSTestStatus used to store the results
  */
final class ScalaJSEventHandler(testStatus: ScalaJSTestStatus) extends EventHandler {
  override def handle(event: Event): Unit = {
    val fingerprint = event.fingerprint()
    val name = event.fullyQualifiedName()
    val status = event.status()
    val selector = event.selector()
    val taskDef = new TaskDef(name, fingerprint, false, Array(selector))
    ScalaJSTestResult.all = ScalaJSTestResult.all :+ taskDef
    status.name() match {
      case "Success" => {
        ScalaJSTestResult.succeeded = ScalaJSTestResult.succeeded :+ taskDef
        testStatus.succeeded = testStatus.succeeded :+ taskDef
      }
      case "Error" => {
        ScalaJSTestResult.errored = ScalaJSTestResult.errored :+ taskDef
        testStatus.errored = testStatus.errored :+ taskDef
      }
      case "Failure" => {
        ScalaJSTestResult.failed = ScalaJSTestResult.failed :+ taskDef
        testStatus.failed = testStatus.failed :+ taskDef
      }
      case "Skipped" => {
        ScalaJSTestResult.skipped = ScalaJSTestResult.skipped :+ taskDef
        testStatus.skipped = testStatus.skipped :+ taskDef
      }
      case "Ignored" => {
        ScalaJSTestResult.ignored = ScalaJSTestResult.ignored :+ taskDef
        testStatus.ignored = testStatus.ignored :+ taskDef
      }
      case "Canceled" => {
        ScalaJSTestResult.canceled = ScalaJSTestResult.canceled :+ taskDef
        testStatus.canceled = testStatus.canceled :+ taskDef
      }
      case "Pending" => {
        ScalaJSTestResult.pending = ScalaJSTestResult.pending :+ taskDef
        testStatus.pending = testStatus.pending :+ taskDef
      }
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