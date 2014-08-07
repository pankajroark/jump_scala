package com.pankaj.jump.ident

import com.pankaj.jump.parser.{IdentParser, UniqueIdentFinder}
import com.pankaj.jump.db.{InvertedIdentIndex, IdentTable}
import com.pankaj.jump.Path

class IdentRecorder(identTable: IdentTable, invertedIdentIndex: InvertedIdentIndex) {
  def record(file: Path, timestamp: Long) {
    val identFinder = new UniqueIdentFinder
    IdentParser.parse(file.toFile, identFinder)
    try {
      invertedIdentIndex.addFileIdents(file.toString, identFinder.idents, timestamp)
    } catch {
      case e: Throwable => e.printStackTrace
    }
  }
}
