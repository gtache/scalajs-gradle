package com.github.gtache

import com.greencatsoft.greenlight._
import com.greencatsoft.greenlight.grammar.Statement

object GreenlightFrameworkTest extends TestSuite {

  "The framework" should "be able to test equality" in {

    (1 + 1) must be (2)

    ("A" + "B") should be ("AB")

    "Scala.js" must not be ("overlooked!")
  }

  It should "be able to check if a collection is empty" in {

    List(1, 2, 3) must not be (empty)

    Seq.empty must be (empty)
  }

  It can "check if an option is defined or not" in {

    val value = Some("value")

    value must be (defined)
    value should not be (empty)

    None must be (empty)
    None should not be (defined)
  }

  It can "also check if a string value is empty" in {

    val emptyValue = ""

    emptyValue must be (defined)
    emptyValue must be (empty)

    val nullValue: String = null

    nullValue should be (empty)
    nullValue should not be (defined)

    val value = "Scala"

    value must not be (empty)
    value should be (defined)
  }

  It can "also check if a collection contains an item or not" in {

    val collection = Seq("a", "b", "c", "d")

    collection must contain ("a")
    collection must contain ("b")

    collection must not contain ("e")
  }

  // You can skip a test case by using 'might' instead of 'should/must/can'.
  It might "not be ready for prime time" in {
    "You" should be ("joking!")
  }

  It can "be used to test if an exception is thrown" in {

    A_[NullPointerException] should be_thrown_in {
      (null).toString
    }

    A_[NullPointerException] should not be_thrown_in {
      println("Scala.js rocks!")
    }
  }
}

object LifeCycleTest extends TestSuite with BeforeAndAfter {

  var specification: Option[String] = None

  before { (s: Statement) =>
    this.specification = Some(s.description)
  }

  after {
    this.specification = None
  }

  "BeforeAndAfter trait" can "be used to be notified before and after a specification" in {
    specification should not be (empty)
    specification foreach { it =>
      it should be ("BeforeAndAfter trait can be used to be notified before and after a specification")
    }
  }
}