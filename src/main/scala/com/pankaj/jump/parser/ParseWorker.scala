package com.pankaj.jump.parser

import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.Path

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorkerThread(dirtQueue: ConcurrentLinkedQueue[Path]) extends Runnable {

  def run(): Unit = {
    while(true) {
      val file = dirtQueue.poll()
      if(file == null) {
        Thread.sleep(1000)
      } else {
        println(Parser.parse(file))
      }
    }
  }
}

