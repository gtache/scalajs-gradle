package main.scalajs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@JSExport
object Test extends js.JSApp {

  @JSExport
  def main(): Unit = {
    val squared = square(10)
    println("Hello ! Square of 10 is " + squared)
  }

  /** Computes the square of an integer.
    * This demonstrates unit testing.
    */
  @JSExport
  def square(x: Int): Int = x * x
}