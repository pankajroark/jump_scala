package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.FileTable
import com.pankaj.jump.util.ThreadActor
import java.util.concurrent.ConcurrentLinkedQueue

// Goes over the file table, compares mod stamp to process stamp
// and if process time < mod time then inserts file into work queue
class DirtFinder(
  fileTable: FileTable,
  dirtProcessor:ThreadActor[Path]
  ) extends (Unit => Unit) {
  def apply(u: Unit) = {
    for( (fi, procTs) <- fileTable.allFiles) {
      if (fi.modStamp > procTs) {
        dirtProcessor.send(fi.path)
        println(s"added ${fi.path}")
      }
    }
  }
}
