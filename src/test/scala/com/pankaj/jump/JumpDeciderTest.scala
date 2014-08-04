package com.pankaj.jump

import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

import com.pankaj.jump.parser._
import com.pankaj.jump.db._
import java.io.{File, PrintWriter}


class JumpDeciderSpec extends FlatSpec with Matchers with MockitoSugar {

  def expand(jShort: JSymbolShort, path: Path): JSymbol =
    jShort.toJSymbol(id => Some(path)).get

  def getJDForContent(content: String, choices:List[JSymbolShort] = Nil): (JumpDecider, Path) = {
    val temp = File.createTempFile("temp",".scala");
    temp.deleteOnExit()
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    val path: Path = Path.fromString(temp.getPath)
    val symbolTable = mock[SymbolTable]
    when(symbolTable.symbolsForName(anyString())).thenReturn(choices)
    val fileTable = mock[FileTable]
    when(fileTable.idForFile(path)).thenReturn(Some(1L))
    when(fileTable.fileForId(anyInt())).thenReturn(Some(path))
    (new JumpDecider(new ParserFactory, symbolTable, fileTable), path)
  }

  "parser" should "track down symbol correctly" in {
    val content = """
    |package com
    |package pankaj.jump
    |
    |import com.pankaj.jump.parser.{JSymbol, Pos, Parser}
    |
    |class JumpDecider(parser: Parser) {
    |  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    |    parser.trackDownSymbol(word, pos)
    |    choices.headOption
    |  }
    |}
    """
    val (jd, path) = getJDForContent(content)
    println(jd.choose("Pos", Pos(path, 7, 33), Nil))
  }

  "jump decider" should "use longest prefix match correctly" in {
    val content = """
    |package com.pankaj.jump
    |
    |import com.pankaj.jump.parser.{JSymbol, Pos, Parser}
    |import path.to.link._
    |
    |object Test {
    | val test: MyType
    |}
    """
    val (jd, path) = getJDForContent(content)
    val pos = PosShort(1, 0, 0)
    val wrongOne = JSymbolShort(List("MyType", "dummy1", "to", "path"), pos, "some")
    val symbol = JSymbolShort(List("MyType", "link", "to", "path"), pos, "some")
    val wrongTwo = JSymbolShort(List("MyType", "dummy2", "to", "path"), pos, "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, symbol, wrongTwo)
    val expected = List(expand(symbol, path))
    val chosen = jd.choose("Pos", Pos(path, 6, 12), choices)
    assert(chosen === expected)
  }

  "jump decider" should "handle renamed imports correctly" in {
    // todo test for the case where data is relooked up from symbol table
    val content = """
    |package com.pankaj.jump
    |
    |import com.pankaj.jump.parser.{JSymbol => Renamed, Pos, Parser}
    |import path.to.link._
    |
    |object Test {
    | val test: Renamed
    |}
    """
    val pos = PosShort(1, 0, 0)
    val wrongOne = JSymbolShort(List("MyType", "link", "to", "path"), pos, "some")
    val symbol = JSymbolShort(List("JSymbol", "parser", "jump", "pankaj", "com"), pos, "some")
    val wrongWithRenamedName = JSymbolShort(List("Renamed", "parser", "jump", "pankaj", "com"), pos, "some")
    val wrongTwo = JSymbolShort(List("MyType", "dummy2", "to", "path"), pos, "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, symbol, wrongWithRenamedName, wrongTwo)
    val (jd, path) = getJDForContent(content, choices)
    val chosen = jd.choose("Renamed", Pos(path, 8, 12), choices)
    val expected = List(expand(symbol, path))
    assert(chosen === expected)
  }

  "jump decider" should "look up symbol in its own package correctly" in {
    val content = """
    |package com.my.pkg
    |
    |import path.to.link._
    |
    |object Dummy {
    | val test: MyType
    |}
    """
    val (jd, path) = getJDForContent(content)
    val pos = PosShort(1, 0, 0)
    val correct = JSymbolShort(List("MyType", "pkg", "my", "com"), pos, "some")
    val wrongOne = JSymbolShort(List("Some", "other", "path"), pos, "some")
    val wrongTwo = JSymbolShort(List("MyType", "dummy2", "to", "path"), pos, "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, correct, wrongTwo)
    val chosen = jd.choose("MyType", Pos(path, 7, 12), choices)
    val expected = List(expand(correct, path))
    assert(chosen === expected)
  }
}
