package com.pankaj.jump

import com.pankaj.jump.parser.{JSymbol, Pos}

class JumpDecider {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    choices.headOption
  }
}

