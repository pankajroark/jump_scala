package com.pankaj.jump

import com.pankaj.jump.parser.{JSymbol, Pos, Parser}

class JumpDecider(parser: Parser) {
  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    parser.trackDownSymbol(word, pos)
    choices.headOption
  }
}

