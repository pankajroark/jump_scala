package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.pankaj.jump.db.RootsTable
import com.twitter.util.{Return, Throw, Try}
import java.io.File

class RootsTracker(rootsTable: RootsTable) {

  // This is ok since list is immutable
  def roots: List[String] = rootsTable.getRoots()

  // We can cache this possibly, check the cached candidates first
  // This one actually goes to disk and checks
  private def findGitRootOnDisk(path: Path): Option[Path] = {
    path.allDirs find { p =>
      (p.appendDir(".git") map { gitPath =>
        val gitDir = new File(gitPath.toString)
        gitDir.exists
      }) getOrElse false
    }
  }

  def track(path: Path): Boolean = {
    findGitRootOnDisk(path) match {
      case Some(root) =>
          rootsTable.addRoot(root)
        true
      case None => false
    }
  }

  // Try to find a git root for path in the table
  def findGitRoot(path: Path): Option[Path] = {
    roots.find(path.toString.startsWith(_)) map { Path.fromString(_) }
  }
}
