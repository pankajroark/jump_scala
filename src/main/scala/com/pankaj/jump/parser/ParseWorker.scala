package com.pankaj.jump.parser

import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.Path
import com.pankaj.jump.db.{FileTable, SymbolTable}
import com.pankaj.jump.fs.FileInfo

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorkerThread(
  dirtQueue: ConcurrentLinkedQueue[Path],
  fileTable: FileTable,
  symbolTable: SymbolTable,
  parser: Parser
) extends Runnable {

  def processFile(file: Path) = {
    println(s"processing ${file.toString}")
    // remove existing entries from Symbol Table
    symbolTable.deleteSymbolsForFile(file)
    val procTs = System.currentTimeMillis
    val symbols = parser.listSymbols(file)
    symbols.foreach { symbolTable.addSymbol(_) }
    fileTable.updateProcessStamp(file, procTs)
    // Mark processed in filetable
  }

  def fileNeedsProcessing(file: Path): Boolean =
    fileTable.fileInfo(file) match {
      case Some((FileInfo(_, modTs), procTs)) if modTs < procTs => false
      case _ => true
    }

  def run(): Unit = {
    while(true) {
      val file = dirtQueue.poll()
      if(file == null) {
        Thread.sleep(1000)
      } else {
        // First, check if the file still needs processing
        // Second, parse file and insert symbols in symbol table
        if(fileNeedsProcessing(file)) processFile(file)
      }
    }
  }
}

