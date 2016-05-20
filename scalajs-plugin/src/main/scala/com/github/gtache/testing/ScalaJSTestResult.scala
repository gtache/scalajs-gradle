package com.github.gtache.testing

import org.scalajs.testadapter.ScalaJSFramework
import sbt.testing.{Runner, TaskDef}

/**
  * An object containing the testing results.
  */
object ScalaJSTestResult {
  var statuses: Set[ScalaJSTestStatus] = Set.empty
  var lastStatuses: Set[ScalaJSTestStatus] = Set.empty

  /**
    * Returns all successful classes
    *
    * @return A set of classnames
    */
  def successfulClassnames: Set[String] = {
    if (isFinished) {
      sanitizeClassnames(getSuccessfulNames).diff(failedClassnames)
    } else {
      println("Testing is not finished")
      Set.empty
    }
  }

  private def sanitizeClassnames(names: Set[String]): Set[String] = {
    names.map(s => s.takeWhile(c => c != ':')).filter(s => !s.contains('#'))
  }

  /**
    * Returns all failed classes
    *
    * @return A set of classnames
    */
  def failedClassnames: Set[String] = {
    if (isFinished) {
      sanitizeClassnames(getErroredNames ++ getFailedNames)
    } else {
      println("Testing is not finished")
      Set.empty
    }
  }

  /**
    * Returns the successful classes from the last run
    *
    * @return A set of classnames
    */
  def getLastSuccessfulClassnames: Set[String] = {
    sanitizeClassnames(lastStatuses.flatMap(s => s.succeeded).map(t => t.fullyQualifiedName())).diff(getLastFailedClassnames)
  }

  /**
    * Returns the failed classes from the last run
    *
    * @return A set of classnames
    */
  def getLastFailedClassnames: Set[String] = {
    sanitizeClassnames((lastStatuses.flatMap(s => s.failed) ++ lastStatuses.flatMap(s => s.errored)).map(t => t.fullyQualifiedName()))
  }

  /**
    * Returns the statuses from the last run
    *
    * @return A set of ScalaJSTestStatus
    */
  def getLastStatuses: Set[ScalaJSTestStatus] = {
    lastStatuses
  }

  /**
    * Saves the statuses in lastStatuses and clear the current ones
    */
  def save(): Unit = {
    lastStatuses = statuses
    statuses = Set.empty
  }

  override def toString: String = {
    "Testing result " + (if (!isFinished) {
      "(testing is not finished !)"
    } else "") + " : " +
      "\n--All : " + getAllNames.toList.sorted.mkString("\n\t") +
      "\n--Success : " + getSuccessfulNames.toList.sorted.mkString("\n\t") +
      "\n--Error : " + getErroredNames.toList.sorted.mkString("\n\t") +
      "\n--Fail : " + getFailedNames.toList.sorted.mkString("\n\t") +
      "\n--Skip : " + getSkippedNames.toList.sorted.mkString("\n\t") +
      "\n--Ignored : " + getIgnoredNames.toList.sorted.mkString("\n\t") +
      "\n--Canceled : " + getCanceledNames.toList.sorted.mkString("\n\t") +
      "\n--Pending : " + getPendingNames.toList.sorted.mkString("\n\t") +
      (if (isSuccess) "\nAll tests passed" else "\nSome tests failed")
  }

  /**
    * Checks if all tests were successful
    *
    * @return true or false
    */
  def isSuccess: Boolean = {
    if (isFinished) {
      getFailedNames.isEmpty && getErroredNames.isEmpty && getCanceledNames.isEmpty
    } else {
      println("Testing is not finished")
      false
    }
  }

  /**
    * Checks if testing is finished
    *
    * @return true or false
    */
  def isFinished: Boolean = {
    !statuses.exists(s => !s.isFinished)
  }

  private def getErroredNames: Set[String] = {
    statuses.flatMap(s => s.errored).map(t => t.fullyQualifiedName())
  }

  private def getFailedNames: Set[String] = {
    statuses.flatMap(s => s.failed).map(t => t.fullyQualifiedName())
  }

  private def getCanceledNames: Set[String] = {
    statuses.flatMap(s => s.canceled).map(t => t.fullyQualifiedName())
  }

  private def getAllNames: Set[String] = {
    statuses.flatMap(s => s.all).map(t => t.fullyQualifiedName())
  }

  private def getSuccessfulNames: Set[String] = {
    statuses.flatMap(s => s.succeeded).map(t => t.fullyQualifiedName())
  }

  private def getSkippedNames: Set[String] = {
    statuses.flatMap(s => s.skipped).map(t => t.fullyQualifiedName())
  }

  private def getIgnoredNames: Set[String] = {
    statuses.flatMap(s => s.ignored).map(t => t.fullyQualifiedName())
  }

  private def getPendingNames: Set[String] = {
    statuses.flatMap(s => s.pending).map(t => t.fullyQualifiedName())
  }

  /**
    * Returns the ScalaJSTestStatus for the given ScalaJSFramework
    *
    * @param f The framework
    * @return An option containing the framework, or None
    */
  def getStatusFor(f: ScalaJSFramework): Option[ScalaJSFramework] = {
    statuses.find(s => f.name == s.framework.name).map(s => s.framework)
  }
}

/**
  * A class storing informations about a TestFramework (results of test)
  *
  * @param framework The framework which corresponds to this instance
  */
final class ScalaJSTestStatus(val framework: ScalaJSFramework) {
  var runner: Runner = null
  var all: List[TaskDef] = List.empty
  var errored: List[TaskDef] = List.empty
  var failed: List[TaskDef] = List.empty
  var succeeded: List[TaskDef] = List.empty
  var skipped: List[TaskDef] = List.empty
  var ignored: List[TaskDef] = List.empty
  var canceled: List[TaskDef] = List.empty
  var pending: List[TaskDef] = List.empty
  var finished = false

  /**
    * Checks if testing is finished
    *
    * @return true or false
    */
  def isFinished: Boolean = {
    finished
  }

  override def toString: String = {
    "ScalaJSTestStatus for " + framework.name + " : " +
      "\n--Runner : " + runner +
      "\n--All : " + all.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Success : " + succeeded.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Error : " + errored.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Fail : " + failed.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Skip : " + skipped.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Ignored : " + ignored.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Canceled : " + canceled.map(t => t.fullyQualifiedName()).mkString("\n\t") +
      "\n--Pending : " + pending.map(t => t.fullyQualifiedName()).mkString("\n\t")
  }
}
