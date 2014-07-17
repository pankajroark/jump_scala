package com.pankaj.jump.fs

import com.pankaj.jump.Path
import com.twitter.util.{Return, Throw, Try}
import java.io.File

class RootsTracker {
  private var _roots = Set.empty[Path]

  // This is ok since list is immutable
  def roots = _roots

  // We can cache this possibly, check the cached candidates first
  private def findGitRoot(path: Path): Option[Path] = {
    path.allDirs find { p =>
      (p.appendDir(".git") map { gitPath => 
        val gitDir = new File(gitPath.toString)
        gitDir.exists
      }) getOrElse false
    }
  }

  def track(path: Path): Boolean = {
    findGitRoot(path) match {
      case Some(root) =>
        this.synchronized {
          _roots = _roots + root
        }
        true
      case None => false
    }
  }
}
