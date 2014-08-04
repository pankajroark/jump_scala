package com.pankaj

import com.twitter.util.{Return, Throw, Try}

package object jump {

  object IntNumber {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

  object Loc {
    def unapply(str: String): Option[(Path, Int, Int)] = {
      str.split(":") match {
        case Array(path, IntNumber(row), IntNumber(col)) =>
              Some((path, row, col))
        case _ => None
      }
    }

    def parseLoc(str: String): Try[Loc] = {
      str match {
        case Loc(path, row, col) =>
          Return(new Loc(path, row, col))
        case _ => Throw(new Exception(s"invalid location $str"))
      }
    }
  }

  implicit def toTryConvertable[A](opt: Option[A]): TryConvertable[A] =
    new TryConvertable(opt)

  class TryConvertable[A](opt: Option[A]) {
    def toTry(e: Throwable): Try[A] = opt match {
      case Some(x) => Return(x)
      case None => Throw(e)
    }
  }

  implicit def fallbackableList[A](xs: List[A]): FallbackableList[A] =
    new FallbackableList(xs)

  class FallbackableList[A](xs: List[A]) {
    // Not naming it orElse to avoid conflict with existing
    // function with that name
    def orElseUse(ys: List[A]): List[A] =
      if(xs.isEmpty) ys
      else xs
  }

  class Loc(path: Path, row: Int, col: Int)

  def hash(s: String): Long = {
    var h = 1125899906842597L
    for (i <- 0 until s.size) {
      h = 31*h + s(i);
    }
    h
  }

}
