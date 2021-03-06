package com.pankaj.jump

import com.pankaj.jump.parser._
import com.pankaj.jump.fs.RootsTracker
import com.pankaj.jump.db.{SymbolTable, FileTable}
import scala.annotation.tailrec

class JumpDecider(
  parserFactory: ParserFactory,
  symbolTable: SymbolTable,
  fileTable: FileTable,
  rootsTracker: RootsTracker
) {
  def choose(word: String, pos: Pos, choices: List[JSymbolShort]): List[JSymbol] = {
    println(choices.mkString("\n"))
    val (imports, pkg) = parserFactory.get.trackDownSymbol(word, pos)

    def tryFindExactMatchWithoutRenames(): List[JSymbolShort] = {
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
      matchingSymbols
    }

    def tryFindExactMatchWithRenames(): List[JSymbolShort] = {
      println("[INFO] tryFindExactMatchWithRenames called")
      for{
       j@JImport(qual, name, rename) <- imports
       if word != name && word == rename
       newChoices = symbolTable.symbolsForName(name)
       choice <- newChoices.find(_.qualName == (name :: qual).reverse.mkString("."))
      } yield {
        choice
      }
    }

    def tryLongestPrefixMatch(): List[JSymbolShort] = {
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

      // Int.MaxValue if not prefix
      // else distance
      // The smaller the distance the better the match
      def prefixDistance(imp: List[String], choice: List[String]): Int = {
        @tailrec
        def go(xs: List[String], ys: List[String], d: Int): Int = {
          (xs, ys) match {
            case (p::ps, q::qs) =>
              if (p == q) go(ps, qs, d)
              else Int.MaxValue
            case (p::ps, Nil) => // import is longer than choice
              Int.MaxValue
            case (Nil, z) => ys.size
          }
        }
        go(imp, choice, 0)
      }

      var bestChoices = Set[JSymbolShort]()
      var choiceCount = Int.MaxValue
      for {
        choice <- choices
        qual <- quals
      } {
        //val count = prefixMatchCount(choice.rfqn.reverse, qual) - qual.size
        val count = prefixDistance(qual, choice.rfqn.tail.reverse)
        //println(s"$count :: $choice :: $qual")
        if (count < choiceCount) {
          choiceCount = count
          bestChoices = Set(choice)
        } else if (count == choiceCount) {
          bestChoices = bestChoices + choice
        }
      }

      println(s"best match is $choiceCount ")
      println(s"best choices are ${bestChoices.mkString("\n")} ")
      // todo choose the one that's closest to package
      /*
      var cc = 0
      var bc: Option[JSymbolShort] = None
      for (choice <- bestChoices) {
        val count = prefixMatchCount(choice.rfqn.reverse, pkg.reverse)
        if (count > cc) {
          cc = count
          bc = Some(choice)
        }
      }
      bc
      */
     bestChoices.toList
    }

    val chosen:List[JSymbolShort] = tryFindExactMatchWithoutRenames() orElseUse
    tryFindExactMatchWithRenames() orElseUse
    tryLongestPrefixMatch() orElseUse choices

    val distinctOnLocation = chosen.groupBy(_.loc).map{_._2.head}.toList
    val chosenSymbols = (distinctOnLocation flatMap { jshort =>
      jshort.toJSymbol{ id => fileTable.fileForId(id) }
    })
    //choices.headOption
    withSameRoot(pos, chosenSymbols)
  }

  def withSameRoot(pos: Pos, choices: List[JSymbol]): List[JSymbol] = {
    // [TODO] find current file's git root
    // find git roots of all choice files
    // Eliminate the ones that have the same origin but different git root

    // For now just find matches under current root
    rootsTracker.findGitRoot(pos.file) match {
      case Some(root) =>
        choices.filter { choice =>
          val file = choice.loc.file
          file.startsWith(root)
        }
      case None => choices
    }


  }
}

