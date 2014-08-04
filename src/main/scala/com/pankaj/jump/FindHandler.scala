package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.parser.Pos
import com.pankaj.jump.ident.IdentSearcher
import com.pankaj.jump.db.{FileTable, RootsTable, SymbolTable}
import java.io.File

class FindHandler(
  rootsTable: RootsTable,
  identSearcher: IdentSearcher
) {
  def find(word: String, file: String, row: Int, col: Int): Try[String] = {
    val pos = Pos(Path.fromString(file), row, col)
    closestRoot(file) match{
      case None => Throw(new Exception("file not under any root"))
      case Some(root) =>
        //println(s"Root >>>>> $root")
        val matches = identSearcher.search(word)
        //println(s"matches >>>>>>>>> $matches")
        val matching = for {
          l <- matches
          if l.file.toString.startsWith(root)
        } yield {
          s"${l.file}:${l.row}:${l.col}"
        }
        if (matching.isEmpty) Throw(new Exception("no matching locations found"))
        else Return(matching.mkString(","))
    }
  }

  def closestRoot(filepath: String): Option[String] = {
    val roots = rootsTable.getRoots
    val matching = roots.filter(filepath.startsWith(_))
    Try(matching.maxBy(_.size)).toOption
  }

}
