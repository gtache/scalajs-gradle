package com.github.gtache.testing

import sbt.testing.TaskDef


object ScalaJSTestResult {
  var all: List[TaskDef] = List.empty
  var errored: List[TaskDef] = List.empty
  var failed: List[TaskDef] = List.empty
  var succeeded: List[TaskDef] = List.empty
  var skipped: List[TaskDef] = List.empty
  var ignored: List[TaskDef] = List.empty
  var canceled: List[TaskDef] = List.empty
  var pending: List[TaskDef] = List.empty

  def isSuccess : Boolean = {
    succeeded.size == all.size
  }

  def successfulClassnames : Set[String] = {
    succeeded.map(t => t.fullyQualifiedName()).toSet
  }

  def failedClassnames : Set[String] = {
    (errored++failed).map(t => t.fullyQualifiedName()).toSet
  }

  def clear : Unit = {
    all = List.empty
    errored = List.empty
    failed = List.empty
    succeeded = List.empty
    skipped = List.empty
    ignored = List.empty
    canceled = List.empty
    pending = List.empty
  }

  override def toString: String = {
    "Testing result : " +
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
