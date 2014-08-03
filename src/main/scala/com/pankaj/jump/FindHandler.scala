package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.parser.Pos
import com.pankaj.jump.db.{FileTable, RootsTable, SymbolTable}
import java.io.File

class FindHandler(
  symbolTable: SymbolTable,
  rootsTable: RootsTable,
  fileTable: FileTable
) {
  def find(word: String, file: String, row: Int, col: Int): Try[String] = {
    val pos = Pos(Path.fromString(file), row, col)
    val choices = symbolTable.symbolsForName(word)
    println(choices)
    closestRoot(file) match{
      case None => Throw(new Exception("file not under any root"))
      case Some(root) =>
        /*
        val matching = choices.filter(_.loc.file.startsWith(root)) map { symbol =>
          val l = symbol.loc
          s"${l.file}:${l.row}:${l.col}"
        }
        */
        val matching = for {
          symbolShort <- choices
          l = symbolShort.loc
          file <- fileTable.fileForId(l.fileId)
          if file.toString.startsWith(root)
        } yield {
          s"$file:${l.row}:${l.col}"
        }
        if (matching.isEmpty) Throw(new Exception("no matching locations found"))
        else Return(matching.mkString(","))
    }
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

  def closestRoot(filepath: String): Option[String] = {
    val roots = rootsTable.getRoots
    val matching = roots.filter(filepath.startsWith(_))
    Try(matching.maxBy(_.size)).toOption
  }

}
