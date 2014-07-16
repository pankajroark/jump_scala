package com.pankaj.jump.parser

import java.util.concurrent.ArrayBlockingQueue
import com.pankaj.jump.Path

// ParserWorker reads dirty files from the dirt queue, parses them and inserts
// qualified symbols in the Symbol table
class ParseWorker(queue: ArrayBlockingQueue[Path]) extends Runnable {

  def run() = {
  }
}

