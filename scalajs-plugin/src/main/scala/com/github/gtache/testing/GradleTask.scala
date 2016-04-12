package com.github.gtache.testing

import sbt.testing.{EventHandler, Logger, Task, TaskDef}


class GradleTask extends Task {
  override def tags(): Array[String] = ???

  override def taskDef(): TaskDef = ???

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    ???
  }

}
