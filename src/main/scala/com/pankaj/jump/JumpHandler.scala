package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import java.io.File

object JumpHandler {
  def handleJump(locStr: String): Try[String] = {
    val loc = Loc.parseLoc(locStr)
    Return("")
  }


  // Find the word under the cursor will all the namespaces
  def findNamespacedCurrentSymbol(loc: Loc): List[String] = {
    // First get the package
    // To from top to bottom finding all namespacing elements
    // class, object, trait, def
    List()
  }
}
