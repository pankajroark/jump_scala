package com.pankaj.jump.ident

import com.pankaj.jump.parser.{IdentParser, UniqueIdentFinder}
import com.pankaj.jump.db.{InvertedIdentIndexTable, IdentTable}
import com.pankaj.jump.Path

class IdentRecorder(identTable: IdentTable, invertedIdentIndexTable: InvertedIdentIndexTable) {
  def record(file: Path) {
    val identFinder = new UniqueIdentFinder
    IdentParser.parse(file.toFile, identFinder)
    try {
      invertedIdentIndexTable.addFileIdents(file.toString, identFinder.idents)
    } catch {
      case e: Throwable => e.printStackTrace
    }
  }
}
