package com.pankaj.jump.parser

import java.io.{File, FileReader, BufferedReader}
import scala.collection.mutable.ListBuffer

trait IdentParserObserver {
  def start()
  def startLine(row: Int)
  def endLine(row: Int)
  def startIdent(col: Int)
  def endIdent(ident: String, col: Int)
  def end()
}

case class Ident(ident: String, row: Int, col: Int)

object IdentCollector {
  val BlackList = Set(
    "abstract",
    "case",
    "catch",
    "class",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "try",
    "true",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield",
    "_"
  )

  val MinIdentifierLength = 4
}

class IdentCollector extends IdentParserObserver {

  var _row = 0
  val _ibs = ListBuffer[Ident]()
  var _idents = List[Ident]()
  var _startIdent = 0

  def idents = _idents

  def start() = {}
  def startLine(row: Int) = {
    _row = row
  }
  def endLine(number: Int) = {}
  def startIdent(col: Int) = {
    _startIdent = col
  }
  def endIdent(ident: String, col: Int) = {
    import IdentCollector._
    if(
      !BlackList.contains(ident) &&
      ident.size >= MinIdentifierLength
    ) {
      // both row and col are 1 based
      _ibs += Ident(ident, _row, _startIdent)
    }
  }

  def end() = {
    _idents = _ibs.toList
  }

}

object IdentParser {
  def parse(file: File, ob: IdentParserObserver): Unit = {
    val reader = new BufferedReader(new FileReader(file))
    var row = 0
    ob.start()
    while(true) {
      val line = reader.readLine
      if (line == null) {
        ob.end()
        return
      }
      row += 1
      ob.startLine(row)
      parseLine(line, ob)
      ob.endLine(row)
    }
  }

  private def parseLine(line: String, ob: IdentParserObserver) = {
    var identStart = false
    var sb = new StringBuilder
    for(i <- 0 until line.size) {
      val ch = line(i)
      if (isIdentChar(ch)) {
        if (!identStart) {
          identStart = true
          sb = new StringBuilder
          ob.startIdent(i + 1)
        }
        sb += ch
      } else {
        if (identStart) {
          identStart = false
          ob.endIdent(sb.toString, i)
        }
      }
    }
    if (identStart) {
      identStart = false // redundant, leaving here for symmetry
      ob.endIdent(sb.toString, line.size)
    }
  }

  private def isIdentChar(ch: Char): Boolean = {
    val chInt = ch.toInt
    (ch >= 48 && ch <= 57) || // 0-9
    (ch >= 65 && ch <= 90) || // A-Z
    (ch >= 97 && ch <= 122) || // a-z
    ch == 36 || // $
    ch == 95  // $
  }
}
