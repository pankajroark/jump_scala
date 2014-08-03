package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.parser.Pos
import com.pankaj.jump.db.SymbolTable
import java.io.File

class FindHandler(symbolTable: SymbolTable) {
  def find(word: String, file: String, row: Int, col: Int): Try[String] = {
    val pos = Pos(Path.fromString(file), row, col)
    val choices = symbolTable.symbolsForName(word)
    Return("")
    //println(choices)
    // Feed them to Jump Decider
    /*
    decider.choose(word, pos, choices) match {
      case Nil => Throw(new Exception("No matching locations found"))
      case symbols =>
        Return((for (symbol <- symbols) yield {
          val l = symbol.loc
          s"${l.file}:${l.row}:${l.col}"
        }).mkString(","))
    }
    */
  }
}
