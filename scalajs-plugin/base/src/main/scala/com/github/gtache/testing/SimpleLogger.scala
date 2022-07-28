package com.github.gtache.testing

import sbt.testing.Logger

/* https://github.com/scala-js/scala-js/blob/02be3eafcce8d2c43ae4b133969a7d5817b74bc8/tools/js/src/test/scala/org/scalajs/core/tools/test/js/TestRunner.scala */

/**
  * A basic Logger for testing
  */
final class SimpleLogger extends Logger {
  def ansiCodesSupported(): Boolean = true

  def error(msg: String): Unit = println(msg)

  def warn(msg: String): Unit = println(msg)

  def info(msg: String): Unit = println(msg)

  def debug(msg: String): Unit = println(msg)

  def trace(t: Throwable): Unit = t.printStackTrace()
}