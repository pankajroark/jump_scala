package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.FileTable
import com.pankaj.jump.util.ThreadActor
import java.io.File

case class FileInfo(path: Path, modStamp: Long)

// Crawl the disk at roots and update mod stamp of files
// in File table
// Only look for java/scala files
// Need to use some kind of periodic task runner
class DiskCrawler(
  rootsTracker: RootsTracker,
  fileTable: FileTable,
  dirtFinder: ThreadActor[Unit]
) extends (Unit => Unit) {
  def apply(u: Unit) {
    println("crawl")
    val roots = rootsTracker.roots
    roots foreach { root =>
      crawl(root, isJavaOrScalaFile _){ fi =>
        // todo use log here instead of println
        // todo insert into File Table here
        fileTable.addOrUpdateFileWithModStamp(fi)
        //println(fi.path)
      }
    }
    dirtFinder.send(())
  }

  def isJavaOrScalaFile(f: File): Boolean =
    f.isFile && {
      val ext = f.getName.split('.').last
      ext == "scala" || ext == "java"
    }

  // Call f on each file under root
  private def crawl(root: Path, filter: File => Boolean)(f: FileInfo => Unit) = {
    def go(dir: File): Unit = {
      for (child <- dir.listFiles) {
        if(filter(child)) {
          f(FileInfo(Path.fromString(child.getPath), child.lastModified))
        } else if (child.isDirectory) {
          go(child)
        }
      }
    }
    go(root.toFile)
  }
}
