package com.github.gtache

import scalaprops.Property.{implies, prop, property}
import scalaprops._

object ScalaPropsFrameworkTest extends Scalaprops {

  val makeList = property { n: Int =>
    implies(n >= 0 && n < 10000, prop(List.fill(n)("").length == n))
  }

  val trivial = property { n: Int => Bool.bool(n == 0).implies(n == 0) }
}