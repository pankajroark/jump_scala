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
}
