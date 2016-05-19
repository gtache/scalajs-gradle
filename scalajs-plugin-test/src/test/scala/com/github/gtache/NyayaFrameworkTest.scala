package com.github.gtache

import nyaya.prop._
import nyaya.test.PropTestOps

object NyayaFrameworkTest extends PropTestOps {

  val p: Prop[AllThings] = Prop.distinct("thing IDs", (_: AllThings).things.map(_.id))

  case class Thing(id: Int, name: String)

  case class AllThings(timestamp: Long, things: List[Thing])
}