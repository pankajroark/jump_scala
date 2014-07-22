package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.parser.Pos
import com.pankaj.jump.db.SymbolTable
import java.io.File

class JumpHandler(decider: JumpDecider, symbolTable: SymbolTable) {
  def jump(word: String, file: String, row: Int, col: Int): Try[String] = {
    val pos = Pos(Path.fromString(file), row, col)
    val choices = symbolTable.symbolsForName(word)
    println(choices)
    // Feed them to Jump Decider
    decider.choose(word, pos, choices) match {
      case Some(symbol) =>
        val l = symbol.loc
        val locStr = s"${l.file}:${l.row}:${l.col}"
        Return(locStr)
      case None => Throw(new Exception("No matching locations found"))
    }
  }


  // Find the word under the cursor will all the namespaces
  // todo for later
  // for now just use the word directly
  def findNamespacedCurrentSymbol(loc: Loc): List[String] = {
    List()
  }
}
