package com.pankaj.jump

import com.pankaj.jump.parser.{JSymbol, Pos}

class JumpDecider {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): JSymbol = {
    JSymbol(Nil, Pos(Path.fromString("/"), 0, 0), "val")
  }
}

