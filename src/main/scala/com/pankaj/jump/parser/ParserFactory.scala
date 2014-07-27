package com.pankaj.jump.parser


class ParserFactory {
  private var _parserUseCount:Int = 0
  private val ParserMaxUseCount: Int = 5000
  private var _parser: Parser = new Parser

  def get: Parser = this.synchronized {
    if (_parserUseCount == ParserMaxUseCount) {
      _parserUseCount = 0
      _parser = new Parser
    } else {
      _parserUseCount += 1
    }
    _parser
  }
}

