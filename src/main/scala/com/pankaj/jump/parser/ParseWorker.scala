package com.pankaj.jump.parser

import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.Path
import com.pankaj.jump.db.{FileTable, SymbolTable}
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.ident.IdentRecorder

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorker(
  fileTable: FileTable,
  symbolTable: SymbolTable,
  parserFactory: ParserFactory,
  identRecorder: IdentRecorder
) extends (Path => Unit) {


  def apply(file: Path): Unit = {
    // First, check if the file still needs processing
    // Second, parse file and insert symbols in symbol table
    if(fileNeedsProcessing(file)) processFile(file)
  }

  private def processFile(file: Path) = {
    println(s"processing ${file.toString}")
    // remove existing entries from Symbol Table
    symbolTable.deleteSymbolsForFile(file)
    val procTs = System.currentTimeMillis
    parserFactory.get.forSymbols(file)(sym => symbolTable.addSymbol(sym))
    identRecorder.record(file, procTs)
    // todo add logic to record identifiers
    fileTable.updateProcessStamp(file, procTs)
    // Mark processed in filetable
  }

  private def fileNeedsProcessing(file: Path): Boolean =
    fileTable.fileInfo(file) match {
      case Some((_, procTs)) if file.toFile.lastModified < procTs => false
      case _ => true
    }

}

