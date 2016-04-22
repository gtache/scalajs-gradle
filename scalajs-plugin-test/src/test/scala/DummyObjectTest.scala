import org.junit.Test
import org.junit.Assert._

import scala.scalajs.js.annotation.JSExport

@JSExport
class DummyObjectTest {

  @Test
  @JSExport
  def dummyTest() : Unit = {
    assertEquals(100,DummyObject.square(10))
  }
}
