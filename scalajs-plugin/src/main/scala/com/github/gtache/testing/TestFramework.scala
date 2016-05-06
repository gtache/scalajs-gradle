package com.github.gtache.testing


/* sbt -- Simple Build Tool
 * Copyright 2008, 2009  Steven Blundy, Mark Harrah, Josh Cough
 */
/* https://github.com/sbt/sbt/blob/83f35b18d348d36611570642833976d0f6520a60/testing/src/main/scala/sbt/TestFramework.scala */

object TestFrameworks {
  val ScalaCheck = new TestFramework("org.scalacheck.ScalaCheckFramework")
  val ScalaTest = new TestFramework("org.scalatest.tools.Framework", "org.scalatest.tools.ScalaTestFramework")
  val Specs = new TestFramework("org.specs.runner.SpecsFramework")
  val Specs2 = new TestFramework("org.specs2.runner.Specs2Framework", "org.specs2.runner.SpecsFramework")
  val JUnit = new TestFramework("com.novocode.junit.JUnitFramework")

  val allFrameworks = Seq(ScalaCheck, ScalaTest, Specs, Specs2, JUnit)
}

/**
  * A simple container for a list of string representing implementing classes for a TestFramework
  * @param classNames The names of the class which correspond to a framework
  */
class TestFramework(val classNames: String*) {

}