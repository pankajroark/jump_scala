package com.pankaj.jump.parser

import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.Path
import com.pankaj.jump.db.SymbolTable

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorkerThread(dirtQueue: ConcurrentLinkedQueue[Path], symbolTable: SymbolTable) extends Runnable {

  def run(): Unit = {
    while(true) {
      val file = dirtQueue.poll()
      if(file == null) {
        Thread.sleep(1000)
      } else {
        // First, check if the file still needs processing
        // Second, parse file and insert symbols in symbol table
        println(Parser.parse(file))
      }
    }
  }
}

