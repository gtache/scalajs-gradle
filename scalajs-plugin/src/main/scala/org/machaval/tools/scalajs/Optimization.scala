package org.machaval.tools.scalajs

sealed abstract class Optimization(val description: String)

object Optimization {
  final case object Fast extends Optimization(description = " - fast")
  final case object Full extends Optimization(description = " - full optimization")

  def values(): Seq[Optimization] = Seq(Fast, Full)
}
