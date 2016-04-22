import org.junit.Test
import org.junit.Assert._

import scala.scalajs.js.annotation.JSExport

class DummyObjectTest {

  @Test
  def dummyTest() : Unit = {
    assertEquals(100,DummyObject.square(10))
  }
}
