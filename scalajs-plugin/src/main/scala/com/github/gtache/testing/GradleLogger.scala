package com.github.gtache.testing

import sbt.testing.Logger


class GradleLogger extends Logger {
  override def ansiCodesSupported(): Boolean = ???

  override def warn(msg: String): Unit = ???

  override def error(msg: String): Unit = ???

  override def debug(msg: String): Unit = ???

  override def trace(t: Throwable): Unit = ???

  override def info(msg: String): Unit = ???
}
