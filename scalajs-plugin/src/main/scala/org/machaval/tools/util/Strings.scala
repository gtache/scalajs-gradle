package org.machaval.tools.util

object Strings {

  def split(what: String, on: Char): (String, Option[String]) = {
    what.lastIndexOf(on) match {
      case -1 => (what, None)
      case index => (what.substring(0, index), Some(what.substring(index + 1)))
    }
  }

  def prefix(prefix: String, what: Option[String]): String = {
    what.fold("")(string => prefix + string)
  }

}
