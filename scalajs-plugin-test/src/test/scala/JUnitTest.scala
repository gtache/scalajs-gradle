import org.junit.Test
import org.junit.Assert._

@Test
class JUnitTest {

  def dummyTest(): Unit = {
    assertEquals(100, DummyObject.square(10))
    assertNotEquals(100, DummyObject.square(5))
  }

  def dummyTest2(): Unit = {
    assertEquals(9, DummyObject.square(3))
  }

}
