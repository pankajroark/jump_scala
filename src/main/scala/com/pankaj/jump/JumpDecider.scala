package com.pankaj.jump

import com.pankaj.jump.parser.{JImport, JSymbol, Pos, Parser}
import com.pankaj.jump.db.SymbolTable
import scala.annotation.tailrec

class JumpDecider(parser: Parser, symbolTable: SymbolTable) {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    val (imports, pkg) = parser.trackDownSymbol(word, pos)

    def tryFindExactMatchWithoutRenames(): Option[JSymbol] = {
      println("[INFO] tryFindExactMatchWithoutRenames called")
      val lookupNames = {
        val importNames = for{
          JImport(qual, name, _) <- imports
          if name == "_" || word == name
        } yield {
          (word :: qual).reverse.mkString(".")
        }

        val packageName = (word :: pkg).reverse.mkString(".")
        packageName :: importNames
      }
      val matchingSymbols = for {
        lName <- lookupNames
        symbol <- choices
        if (symbol.qualName == lName)
      } yield symbol
      matchingSymbols.headOption
    }

    def tryFindExactMatchWithRenames(): Option[JSymbol] = {
      println("[INFO] tryFindExactMatchWithRenames called")
       (for{
          JImport(qual, name, rename) <- imports
          if word != name && word == rename
          newChoices = symbolTable.symbolsForName(name)
          choice <- newChoices.find(_.qualName == (name :: qual).reverse.mkString("."))
        } yield {
          choice
        }).headOption
    }

    def tryLongestPrefixMatch(): Option[JSymbol] = {
      println("[INFO] tryLongestPrefixMatch called")
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
            case (p::ps, q::qs) if (p == q) => go(ps, qs, count + 1)
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

    tryFindExactMatchWithoutRenames() orElse tryFindExactMatchWithRenames() orElse tryLongestPrefixMatch() orElse choices.headOption
    //choices.headOption
  }
}

