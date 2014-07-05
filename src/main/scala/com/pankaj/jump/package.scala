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

  class Loc(path: Path, row: Int, col: Int)

}
