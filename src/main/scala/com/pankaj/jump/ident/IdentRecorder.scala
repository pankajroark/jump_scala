package com.pankaj.jump.ident

import com.pankaj.jump.parser.{IdentParser, UniqueIdentFinder}
import com.pankaj.jump.db.{InvertedIdentIndexTable, IdentTable}
import com.pankaj.jump.Path

class IdentRecorder(identTable: IdentTable, invertedIdentIndexTable: InvertedIdentIndexTable) {
  def record(file: Path) {
    val identFinder = new UniqueIdentFinder
    IdentParser.parse(file.toFile, identFinder)
    for (ident <- identFinder.idents) {
      try{
        identTable.addIdent(ident)
      } catch {
        case e: Throwable => e.printStackTrace
      }
      try {
        invertedIdentIndexTable.addFileIdent(file.toString, ident)
      } catch {
        case e: Throwable => e.printStackTrace
      }
    }
  }
}
