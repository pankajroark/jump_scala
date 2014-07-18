package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.FileTable
import java.util.concurrent.ConcurrentLinkedQueue

// Goes over the file table, compares mod stamp to process stamp
// and if process time < mod time then inserts file into work queue
class DirtFinder(fileTable: FileTable, dirtQueue: ConcurrentLinkedQueue[Path]) {
  def run() = {
    for( (fi, procTs) <- fileTable.allFiles) {
      if (fi.modStamp > procTs) {
        dirtQueue.add(fi.path)
        println(s"added ${fi.path}")
      }
    }
  }
}
