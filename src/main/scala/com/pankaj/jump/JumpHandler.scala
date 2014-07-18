package com.pankaj.jump

import com.twitter.util.{Return, Throw, Try}
import com.pankaj.jump.parser.Pos
import java.io.File

class JumpHandler(decider: JumpDecider) {
  def jump(word: String, file: String, row: Int, col: Int): Try[String] = {
    val pos = Pos(Path.fromString(file), row, col)
    // get matching symbols
    // Feed them to Jump Decider
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
