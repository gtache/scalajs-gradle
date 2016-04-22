import scala.scalajs.js.annotation.JSExport
import scalatags.Text.all._
@JSExport
object DummyScalaTag {
  @JSExport
  def main(): Unit = {
    val page = html(
      head(
        script(src:="..."),
        script(
          "alert('Hello World')"
        )
      ),
      body(
        div(
          h1(id:="title", "This is a title"),
          p("This is a big paragraph of text")
        )
      )
    )
    println(page.toString)
  }
}