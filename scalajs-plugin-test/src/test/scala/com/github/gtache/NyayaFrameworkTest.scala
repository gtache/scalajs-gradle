package com.github.gtache

import nyaya.prop._

object NyayaFrameworkTest {

  case class Thing(id: Int, name: String)
  case class AllThings(timestamp: Long, things: List[Thing])

  val p: Prop[AllThings] = Prop.distinct("thing IDs", (_: AllThings).things.map(_.id))
}
