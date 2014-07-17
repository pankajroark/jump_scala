package com.pankaj.jump

import scala.annotation.tailrec

object Path {
  implicit def fromString(str: String): Path = {
    if (!str.startsWith("/"))
      throw new IllegalArgumentException("invalid path")
    Path(str.split("/").tail)
  }
}

case class Path(parts: Seq[String]) {
  override def toString(): String = "/" + parts.mkString("/")

  def isDir: Boolean = !parts.last.contains(".")

  def prependDir(dir: String) = Path(dir +: parts)

  def appendDir(dir: String): Option[Path] = 
    if (isDir) Some(Path(parts :+ dir))
    else None

  def allDirs: Seq[Path] = {
    val rParts = parts.reverse
    val dirs = if (!rParts.head.contains(".")) {
      rParts
    } else rParts.tail


    @tailrec 
    def go(xs: List[String], acc: List[Path]): List[Path] = xs match {
      case Nil => acc
      case y :: ys => go(ys, Path(xs.reverse) :: acc)
    }

    go(rParts.toList, Nil).reverse
  }
}