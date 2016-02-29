import scala.scalajs.js
import js.annotation.JSExport

@JSExport
object Test2 extends js.JSApp {
  @JSExport
  def main(): Unit = {
    val squared = square(3)
    println("Hello ! Square of 10 is "+squared)
  }

  /** Computes the square of an integer.
    *  This demonstrates unit testing.
    */
  @JSExport
  def square(x: Int): Int = x*x

  @JSExport
  def printSomething(s : String) : Unit = {
    println(s)
  }
}