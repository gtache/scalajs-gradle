package com.github.gtache

/* TODO
import zcheck.SpecLite

object ZCheckFrameworkTest extends SpecLite {

  "Tests should" should {

    "check assertions" in {
      check(10 > 5)
    }

    "check exceptions" in {
      try {
        throw new Error
        fail("should have thrown")
      } catch {
        case _: Throwable => // ok
      }
    }

    "be 0 for an empty map" in {
      Seq().size must_== 0
      Seq().size mustBe_< 1
    }

    "check exceptions are thrown" in {
      //enrichAny(this)
      err(-1).mustThrowA[Error]

      //{err} .mustThrowA[Error]
    }

    "fail if wrong exceptions are thrown" in {
      try {
        err(-1).mustThrowA[RuntimeException]
        fail("should have thrown")
      }
      catch {
        case _: Throwable => // ok
      }
    }
  }

  def err(i: Int) =  if (i < 0) throw new Error else i

}
*/