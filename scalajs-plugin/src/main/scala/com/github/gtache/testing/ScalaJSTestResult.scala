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
      "\n--All : " + getAll.mkString("\n\t") +
      "\n--Success : " + getSuccessful.mkString("\n\t") +
      "\n--Error : " + getErrored.mkString("\n\t") +
      "\n--Fail : " + getFailed.mkString("\n\t") +
      "\n--Skip : " + getSkipped.mkString("\n\t") +
      "\n--Ignored : " + getIgnored.mkString("\n\t") +
      "\n--Canceled : " + getCanceled.mkString("\n\t") +
      "\n--Pending : " + getPending.mkString("\n\t")
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
