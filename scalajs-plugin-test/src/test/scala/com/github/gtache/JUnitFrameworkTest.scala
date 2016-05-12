package com.github.gtache

import org.junit.Assert._
import org.junit.Test


@Test
class JUnitFrameworkTest {

  @Test
  def dummyTest(): Unit = {
    assertEquals(100, DummyObject.square(10))
    assertNotEquals(100, DummyObject.square(5))
  }

  @Test
  def dummyTest2(): Unit = {
    assertEquals(9, DummyObject.square(3))
  }

}
