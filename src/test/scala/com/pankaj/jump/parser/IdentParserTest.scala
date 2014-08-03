package com.pankaj.jump.parser

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.pankaj.jump._
import java.io.{File, PrintWriter}


class IdentParserSpec extends FlatSpec with Matchers {
  def getFileWithContent(content: String): File = {
    val temp = File.createTempFile("temp",".scala");
    temp.deleteOnExit()
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    temp
  }

  "parser" should "parse correctly when start and end with identifier" in {
    val content = """val identifierA = identifierB.func"""

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("identifierA", 1, 5),
      Ident("identifierB", 1, 19),
      Ident("func", 1, 31)
    )
    assert(expected === ic.idents)
  }

  "parser" should "parse correctly when identifier begins with _" in {
    val content = """ _idt = 30"""

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("_idt", 1, 2),
      Ident("30", 1, 9)
    )
    assert(expected === ic.idents)
  }

  "parser" should "parse correctly when identifier not at beginning" in {
    val content = """ { _idt = 30 } """

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("_idt", 1, 4),
      Ident("30", 1, 11)
    )
    assert(expected === ic.idents)
  }

  "parser" should "parse correctly when multiplelines" in {
    val content = "_idtA = 30\n_idtB = 40"

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("_idtA", 1, 1),
      Ident("30", 1, 9),
      Ident("_idtB", 2, 1),
      Ident("40", 2, 9)
    )
    assert(expected === ic.idents)
  }
}
