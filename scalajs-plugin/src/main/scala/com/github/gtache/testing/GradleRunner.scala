package com.github.gtache.testing

import org.scalajs.jsenv.ComJSRunner
import sbt.testing.{Runner, Task, TaskDef}

import scala.collection.concurrent.TrieMap


class GradleRunner(framework: GradleFramework,
                   val args: Array[String],
                   val remoteArgs: Array[String]) extends Runner {
  val slaves = TrieMap.empty[Long, ComJSRunner]
  var master: ComJSRunner = null

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = ???

  override def done(): String = ???

}
