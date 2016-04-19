package com.github.gtache.testing

import sbt.testing.{Runner, Task, TaskDef}

object ScalaJSTestStatus {
  var runner: Runner = null
  var all: Array[Task] = Array.empty
  var errored: Array[TaskDef] = Array.empty
  var failed: Array[TaskDef] = Array.empty
  var succeeded: Array[TaskDef] = Array.empty
  var skipped: Array[TaskDef] = Array.empty
  var ignored: Array[TaskDef] = Array.empty
  var canceled: Array[TaskDef] = Array.empty
  var pending: Array[TaskDef] = Array.empty

  def testingFinished(): Unit = {
    if (runner != null) {
      runner.done()
    }
  }

  override def toString: String = {
    "ScalaJSTestMemory : " +
      "Runner : " + runner +
      "All : " + all +
      "Success : " + succeeded +
      "Error : " + errored +
      "Fail : " + failed +
      "Skip : " + skipped +
      "Ignored : " + ignored +
      "Canceled : " + canceled +
      "Pending : " + pending
  }
}
