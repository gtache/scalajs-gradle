import scala.scalajs.js
import js.annotation.JSExport

@JSExport
object ScalaJSExample extends js.JSApp {
  def main(): Unit = {
    val squared = square(10)
    println("Hello ! Square of 10 is "+squared)
  }

  /** Computes the square of an integer.
    *  This demonstrates unit testing.
    */
  def square(x: Int): Int = x*x
}