package com.pankaj.jump.parser

import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.Path
import com.pankaj.jump.db.{FileTable, SymbolTable}
import com.pankaj.jump.fs.FileInfo

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorker(
  fileTable: FileTable,
  symbolTable: SymbolTable,
  parser: Parser
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
    parser.forSymbols(file)(sym => symbolTable.addSymbol(sym))
    fileTable.updateProcessStamp(file, procTs)
    // Mark processed in filetable
  }

  private def fileNeedsProcessing(file: Path): Boolean =
    fileTable.fileInfo(file) match {
      case Some((_, procTs)) if file.toFile.lastModified < procTs => false
      case _ => true
    }

}

