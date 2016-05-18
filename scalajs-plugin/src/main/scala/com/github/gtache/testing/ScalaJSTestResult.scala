package com.github.gtache.testing

import org.scalajs.testadapter.ScalaJSFramework
import sbt.testing.{Runner, TaskDef}

/**
  * An object containing the previous testing results.
  */
object ScalaJSTestResult {
  var statuses: Set[ScalaJSTestStatus] = Set.empty

  /**
    * Checks if all tests were successful
    *
    * @return true or false
    */
  def isSuccess: Boolean = {
    if (isFinished) {
      getAll.size == getSuccessful.size
    } else {
      println("Testing is not finished")
      println(statuses.filter(s => !s.isFinished).flatMap(s => s.all).map(t => t.fullyQualifiedName()).mkString)
      false
    }
  }

  /**
    * Returns all successful classes
    *
    * @return A set of classnames
    */
  def successfulClassnames: Set[String] = {
    if (isFinished) {
      getSuccessful
    } else {
      println("Testing is not finished")
      Set.empty
    }
  }

  /**
    * Returns all failed classes
    *
    * @return A set of classnames
    */
  def failedClassnames: Set[String] = {
    if (isFinished) {
      getErrored ++ getFailed
    } else {
      println("Testing is not finished")
      Set.empty
    }
  }

  /**
    * Clears the results
    */
  def clear(): Unit = {
    statuses = Set.empty
  }

  override def toString: String = {
    "Testing result " + (if (!isFinished) {
      "(testing is not finished !)"
    }) + " : " +
      "\nAll : " + getAll.mkString +
      "\nSuccess : " + getSuccessful.mkString +
      "\nError : " + getErrored.mkString +
      "\nFail : " + getFailed.mkString +
      "\nSkip : " + getSkipped.mkString +
      "\nIgnored : " + getIgnored.mkString +
      "\nCanceled : " + getCanceled.mkString +
      "\nPending : " + getPending.mkString
  }

  /**
    * Checks if testing is finished
    *
    * @return true or false
    */
  def isFinished: Boolean = {
    !statuses.exists(s => !s.isFinished)
  }

  private def getAll: Set[String] = {
    statuses.flatMap(s => s.all).map(t => t.fullyQualifiedName())
  }

  private def getSuccessful: Set[String] = {
    statuses.flatMap(s => s.succeeded).map(t => t.fullyQualifiedName())
  }

  private def getFailed: Set[String] = {
    statuses.flatMap(s => s.failed).map(t => t.fullyQualifiedName())
  }

  private def getErrored: Set[String] = {
    statuses.flatMap(s => s.errored).map(t => t.fullyQualifiedName())
  }

  private def getSkipped: Set[String] = {
    statuses.flatMap(s => s.skipped).map(t => t.fullyQualifiedName())
  }

  private def getIgnored: Set[String] = {
    statuses.flatMap(s => s.ignored).map(t => t.fullyQualifiedName())
  }

  private def getCanceled: Set[String] = {
    statuses.flatMap(s => s.canceled).map(t => t.fullyQualifiedName())
  }

  private def getPending: Set[String] = {
    statuses.flatMap(s => s.pending).map(t => t.fullyQualifiedName())
  }
}

/**
  * A class storing informations about a TestFramework (results of test)
  *
  * @param framework The framework which corresponds to this instance
  */
final class ScalaJSTestStatus(framework: ScalaJSFramework) {
  var runner: Runner = null
  var all: List[TaskDef] = List.empty
  var errored: List[TaskDef] = List.empty
  var failed: List[TaskDef] = List.empty
  var succeeded: List[TaskDef] = List.empty
  var skipped: List[TaskDef] = List.empty
  var ignored: List[TaskDef] = List.empty
  var canceled: List[TaskDef] = List.empty
  var pending: List[TaskDef] = List.empty
  private var finished = false

  /**
    * Tells the runner / framework that the testing is finished
    */
  def testingFinished(): Unit = {
    if (runner != null && !finished) {
      println(framework.name + " DONE")
      runner.done()
      finished = true
    }
  }

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
      "\nRunner : " + runner +
      "\nAll : " + all.map(t => t.fullyQualifiedName()).mkString +
      "\nSuccess : " + succeeded.map(t => t.fullyQualifiedName()).mkString +
      "\nError : " + errored.map(t => t.fullyQualifiedName()).mkString +
      "\nFail : " + failed.map(t => t.fullyQualifiedName()).mkString +
      "\nSkip : " + skipped.map(t => t.fullyQualifiedName()).mkString +
      "\nIgnored : " + ignored.map(t => t.fullyQualifiedName()).mkString +
      "\nCanceled : " + canceled.map(t => t.fullyQualifiedName()).mkString +
      "\nPending : " + pending.map(t => t.fullyQualifiedName()).mkString
  }
}
