package com.github.gtache

import utest._

object UTestFrameworkTest extends TestSuite {
  val tests = this {
    'test1 {
      2==1
    }
    'test2 {
      1
    }
    'test3 {
      val a = List[Byte](1, 2)
      a(1)==2
    }
  }
}