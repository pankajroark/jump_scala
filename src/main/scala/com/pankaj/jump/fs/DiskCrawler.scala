package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.FileTable
import com.pankaj.jump.util.ThreadActor
import java.io.File
import scala.annotation.tailrec

case class FileInfo(path: Path, modStamp: Long)

object DiskCrawler {
  def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    // Starting from the back go character by character
    val numExtensions = extensions.size
    var extnRejected = new Array[Boolean](numExtensions)
    var fileNameLength = fileName.size
    var extnLengths = extensions map { _.size - 1}
    var allRejected = false
    for (cursor <- 0 until fileNameLength) {
      //println(cursor)
      allRejected = true
      val fileChar = fileName(fileNameLength - 1 - cursor)
      //println(s"fileChar $fileChar")
      for (extnIdx <- 0 until numExtensions) {
        // for one extension
        val extension = extensions(extnIdx)
        val extnLength = extension.size
        if (!extnRejected(extnIdx)) {
          if(cursor == extnLength) {
            if (fileChar == '.') {
              // extension not rejected and reached end so it's a match!!
              return true
            } else {
              extnRejected(extnIdx) = true
            }
          } else {
            val extnChar = extension(extnLength - 1 - cursor)
            //println(s"extnChar $extnChar")
            if (fileChar == extnChar) {
              allRejected = false
            } else {
              extnRejected(extnIdx) = true
            }
          }
        }
      }
      if (allRejected) return false
    }
    true
  }
}

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
    f.isFile && DiskCrawler.hasExtension(f.getName, Array("java", "scala"))

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
