package com.github.gtache.testing

import org.scalajs.core.tools.logging.Logger


class LoggerWrapper(console : Logger) extends sbt.testing.Logger {
  override def warn(msg: String): Unit = console.warn(msg)

  override def error(msg: String): Unit = console.error(msg)

  override def ansiCodesSupported(): Boolean = true

  override def debug(msg: String): Unit = console.debug(msg)

  override def trace(t: Throwable): Unit = console.trace(t)

  override def info(msg: String): Unit = console.info(msg)
}
