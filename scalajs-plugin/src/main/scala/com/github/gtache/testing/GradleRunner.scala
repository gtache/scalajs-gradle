package com.github.gtache.testing

import org.scalajs.jsenv.ComJSRunner
import sbt.testing.{Runner, Task, TaskDef}

import scala.collection.concurrent.TrieMap


class GradleRunner extends Runner{
  var master : ComJSRunner = null
  val slaves = TrieMap.empty[Long, ComJSRunner]
  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = ???

  override def serializeTask(task: Task, serializer: (TaskDef) => String): String = ???

  override def remoteArgs(): Array[String] = ???

  override def deserializeTask(task: String, deserializer: (String) => TaskDef): Task = ???

  override def done(): String = ???

  override def args: Array[String] = ???

  override def receiveMessage(msg: String): Option[String] = ???
}
