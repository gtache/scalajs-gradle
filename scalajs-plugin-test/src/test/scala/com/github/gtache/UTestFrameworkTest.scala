package com.github.gtache

import utest._

object UTestFrameworkTest extends TestSuite {
  val tests = this {
    'test1 {
      assert(true)
    }
    'test2 {
      1 == 1
    }
    'test3 {
      val a = List[Byte](1, 2)
      a(1) == 2
    }
  }
}
