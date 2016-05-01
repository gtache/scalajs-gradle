import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport
object DummyTest extends JSApp {
  @JSExport
  override def main(): Unit = println("test is on classpath")
}
