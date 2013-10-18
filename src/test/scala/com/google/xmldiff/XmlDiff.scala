package com.google.xmldiff

import scala.xml.Elem

abstract class XmlDiff {
  def isSimilar = true
}

/** Documents are similar */
case object NoDiff extends XmlDiff

/** The difference between two nodes. */
case class Diff(path: List[Elem], err: String) extends XmlDiff {
  override def isSimilar = false

  override def toString = {
    val sb = new StringBuilder
    sb.append("Diff at ")
    for (p <- path.reverse) p.nameToString(sb.append('/'))
    sb.append(": ").append(err)
    sb.toString
  }
}