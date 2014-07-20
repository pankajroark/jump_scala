package com.pankaj.jump

import com.pankaj.jump.parser.{JImport, JSymbol, Pos, Parser}

class JumpDecider(parser: Parser) {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    val (imports, pkg) = parser.trackDownSymbol(word, pos)
    println(choices)
    choices.foreach { x => println(x.qualName)}

    def tryFindExactMatch(): Option[JSymbol] = {
      val lookupNames = {
        val importNames = for{
          JImport(qual, name, rename) <- imports
          if(rename == word)
        } yield {
          (rename :: qual).reverse.mkString(".")
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
    tryFindExactMatch()
    //choices.headOption
  }
}

