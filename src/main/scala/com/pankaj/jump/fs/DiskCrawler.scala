package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.FileTable
import com.pankaj.jump.util.ThreadActor
import java.io.File
import scala.annotation.tailrec

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

  val Megabyte: Int = 1024 * 1024

  def apply(u: Unit) {
    println("crawl")
    val roots = rootsTracker.roots
    roots foreach { root =>
      crawl(root){ f =>
        if (isJavaOrScalaFile(f) && f.length < Megabyte) {
          fileTable.addOrUpdateFileWithModStamp(f)
        }
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
  private def crawl(root: Path)(fun: File => Unit) = {
    def go(dir: File): Unit = {
      for (child <- dir.listFiles) {
        fun(child)
        if(child.isFile) {
          fun(child)
        } else if (child.isDirectory) {
          go(child)
        }
      }
    }
    go(root.toFile)
  }
}
