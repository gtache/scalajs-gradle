package com.github.gtache.testing

import sbt.testing._

/**
  * Handles events fired by a Framework
  *
  * @param testStatus The ScalaJSTestStatus used to store the results
  */
final class ScalaJSEventHandler(testStatus: ScalaJSTestStatus) extends EventHandler {
  private var counter = 0

  override def handle(event: Event): Unit = {
    val fingerprint = event.fingerprint()
    val status = event.status()
    val selector = event.selector()

    var name = selector match {
      case n: NestedTestSelector =>
        n.suiteId() + '.' + n.testName()
      case t: TestSelector =>
        t.testName()
      case s: SuiteSelector =>
        event.fullyQualifiedName
      case n: NestedSuiteSelector =>
        n.suiteId()
      case t: TestWildcardSelector =>
        t.testWildcard()
      case _ => throw new IllegalArgumentException("Unknown Selector")
    }

    name = name match {
      case "" => event.fullyQualifiedName() match {
        case "" => {
          counter += 1
          "Unknown test #" + counter
        }
        case _ => event.fullyQualifiedName()
      }
      case _ => name
    }
    println("FP : " + fingerprint)
    println("result : " + status.name)
    println("selector : " + selector)
    println("name : " + name)
    val taskDef = new TaskDef(name, fingerprint, false, Array(selector))
    status.name() match {
      case "Success" =>
        testStatus.succeeded = testStatus.succeeded :+ taskDef

      case "Error" =>
        testStatus.errored = testStatus.errored :+ taskDef

      case "Failure" =>
        testStatus.failed = testStatus.failed :+ taskDef

      case "Skipped" =>
        testStatus.skipped = testStatus.skipped :+ taskDef

      case "Ignored" =>
        testStatus.ignored = testStatus.ignored :+ taskDef

      case "Canceled" =>
        testStatus.canceled = testStatus.canceled :+ taskDef

      case "Pending" =>
        testStatus.pending = testStatus.pending :+ taskDef

      case s: String => throw new IllegalStateException("Unknown task status : " + s)
    }

    println("\n" + testStatus + "\n")

  }
}