package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import java.io.File

object JumpHandler {
  def handleJump(locStr: String): Try[String] = {
    val loc = Loc.parseLoc(locStr)
    Return("")
  }

  // We can cache this possibly, check the cached candidates first
  def findGitRoot(path: Path): Option[Path] = {
    path.allDirs find { p =>
      (p.appendDir(".git") map { gitPath => 
        val gitDir = new File(gitPath.toString)
        gitDir.exists
      }) getOrElse false
    }
  }

  // Find the word under the cursor will all the namespaces
  def findNamespacedCurrentSymbol(loc: Loc): List[String] = {
    // First get the package
    // To from top to bottom finding all namespacing elements
    // class, object, trait, def
    List()
  }
}
