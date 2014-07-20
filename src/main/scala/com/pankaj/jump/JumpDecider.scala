package com.pankaj.jump

import com.pankaj.jump.parser.{JImport, JSymbol, Pos, Parser}
import scala.annotation.tailrec

class JumpDecider(parser: Parser) {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    val (imports, pkg) = parser.trackDownSymbol(word, pos)
    println(choices)
    choices.foreach { x => println(x.qualName)}

    def tryFindExactMatch(): Option[JSymbol] = {
      val lookupNames = {
        val importNames = for{
          JImport(qual, name, rename) <- imports
          if(rename == word || name == "_")
        } yield {
          (word :: qual).reverse.mkString(".")
        }
        val packageName = (word :: pkg.reverse).reverse.mkString(".")
        packageName :: importNames
      }
      println(s"lookup names $lookupNames")
      val matchingSymbols = for {
        lName <- lookupNames
        symbol <- choices
        if (symbol.qualName == lName)
      } yield symbol
      matchingSymbols.headOption
    }

    def tryLongestPrefixMatch(): Option[JSymbol] = {
      val quals = {
        val importQuals = {
          for {
            JImport(qual, name, rename) <- imports
          } yield {
            if (name == "_") qual.reverse
            else (name :: qual).reverse
          }
        }
        pkg.reverse :: importQuals
      }

      def prefixMatchCount(xs: List[String], ys: List[String]): Int = {
        @tailrec
        def go(as: List[String], bs: List[String], count: Int): Int = {
          (as, bs) match {
            case (p::ps, q::qs) => go(ps, qs, count + 1)
            case _ => count
          }
        }
        go(xs, ys, 0)
      }

      var bestChoice = choices.headOption
      var choiceCount = 0
      for {
        choice <- choices
        qual <- quals
      } {
        val count = prefixMatchCount(choice.rfqn.reverse, qual)
        if (count > choiceCount) {
          choiceCount = count
          bestChoice = Some(choice)
        }
      }
      bestChoice
    }

    tryFindExactMatch() orElse tryLongestPrefixMatch() orElse choices.headOption
    //choices.headOption
  }
}

