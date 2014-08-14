package com.pankaj.jump

import scala.annotation.tailrec
import java.io.File

object Path {
  implicit def fromString(str: String): Path = {
    if (!str.startsWith("/"))
      throw new IllegalArgumentException("invalid path")
    Path(str.split("/").tail)
  }
  implicit object PathOrdering extends Ordering[Path] {
    def compare(a: Path, b: Path) = a.parts.mkString compare b.parts.mkString
  }

  def commonPrefix(p1: Path, p2: Path): Path = {
    val commonPartPairs = (p1.parts zip p2.parts).takeWhile(Function.tupled(_ == _))
    Path(commonPartPairs.map(_._1))
  }
}

case class Path(parts: Seq[String]) {
  override def toString(): String = "/" + parts.mkString("/")

  def isDir: Boolean = !filename.contains(".")

  def filename: String = parts.last

  def fileBasename: String = filename.split('.').head

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

    go(dirs.toList, Nil).reverse
  }

  def toFile: File = new File(toString())

  def extension: Option[String] =
    if (!filename.contains(".")) None
    else Some(filename.split('.').last)

  def parent: Option[Path] =
    if (parts.isEmpty) None
    else Some(Path(parts.init))

  def size: Int = parts.size

  def startsWith(p2: Path): Boolean = {
    @tailrec
    def go(big: List[String], small: List[String]): Boolean = {
      (big, small) match {
        case (x::xs, y::ys) => if (x == y) go(xs, ys) else false
        case (Nil, y::ys) => false
        case _ => true
      }
    }
    go(parts.toList, p2.parts.toList)
  }
}
