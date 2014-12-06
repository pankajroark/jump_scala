package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.db.{FileTable, RootsTable, SymbolTable}
import java.io.File

class FindHandler(
  rootsTable: RootsTable
) {
  def closestRoot(filepath: String): Option[String] = {
    val roots = rootsTable.getRoots
    val matching = roots.filter(filepath.startsWith(_))
    Try(matching.maxBy(_.size)).toOption
  }

}
