package com.github.gtache.testing

import sbt.testing._

/**
  * Handles events fired by a Framework
  *
  * @param testStatus The ScalaJSTestStatus used to store the results
  */
final class ScalaJSEventHandler(testStatus: ScalaJSTestStatus) extends EventHandler {
  private var counterUnknown = 0
  private var counterKnown = 0

  override def handle(event: Event): Unit = {
    val fingerprint = event.fingerprint()
    val status = event.status()
    val selector = event.selector()

    var name = selector match {
      case n: NestedTestSelector =>
        n.suiteId() + ':' + n.testName()
      case t: TestSelector =>
        event.fullyQualifiedName() + ':' + t.testName()
      case s: SuiteSelector =>
        event.fullyQualifiedName
      case n: NestedSuiteSelector =>
        event.fullyQualifiedName() + '.' + n.suiteId()
      case t: TestWildcardSelector =>
        t.testWildcard()
      case _ => throw new IllegalArgumentException("Unknown Selector")
    }

    name = name match {
      case "" => event.fullyQualifiedName() match {
        case "" =>
          counterUnknown += 1
          "Unknown test (probably utest) #" + counterUnknown
        case _ => event.fullyQualifiedName()
      }
      case ":" =>
        counterUnknown += 1
        "Unknown test (probably utest) #" + counterUnknown
      case _ => name
    }
    if (testStatus.all.map(t => t.fullyQualifiedName()).contains(name)) {
      counterKnown += 1
      name += ":" + counterKnown
    }
    val taskDef = new TaskDef(name, fingerprint, false, Array(selector))
    testStatus.all = testStatus.all :+ taskDef
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
  }
}