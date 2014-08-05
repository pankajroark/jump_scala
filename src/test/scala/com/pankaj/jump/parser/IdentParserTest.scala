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
    val content = """ _idtA = 30"""

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("_idtA", 1, 2)
    )
    assert(expected === ic.idents)
  }

  "parser" should "parse correctly when identifier not at beginning" in {
    val content = """ { _idtA = 30 } """

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("_idtA", 1, 4)
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
      Ident("_idtB", 2, 1)
    )
    assert(expected === ic.idents)
  }

  "parser" should "ignore imports" in {
    val content = "import identA\nidentA"

    val file = getFileWithContent(content)
    val ic = new IdentCollector
    IdentParser.parse(file, ic)
    val expected = List(
      Ident("identA", 2, 1)
    )
    assert(expected === ic.idents)
  }

  "parser" should "ignore commented lines" in {
    val file = new
    File("/Users/pankajg/workspace/bc3/commerce/card-server/src/main/scala/com/twitter/promotion/config/CardsServerConfig.scala")
    val ic = new UniqueIdentFinder
    IdentParser.parse(file, ic)
    println(ic.idents)
  }
}
