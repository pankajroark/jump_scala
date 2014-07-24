package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import com.pankaj.jump.parser._
import com.pankaj.jump.db._
import java.io.{File, PrintWriter}


class JumpDeciderSpec extends FlatSpec with Matchers {

  def getJDForContent(content: String): (JumpDecider, Path) = {
    val temp = File.createTempFile("temp",".scala");
    temp.deleteOnExit()
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    val path: Path = Path.fromString(temp.getPath)
    val db = new Db
    val symbolTable = new SymbolTable(db)
    val parser = new Parser
    (new JumpDecider(parser, symbolTable), path)
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
    val wrongOne = JSymbol(List("MyType", "dummy1", "to", "path"), Pos(path, 0, 0), "some")
    val symbol = JSymbol(List("MyType", "link", "to", "path"), Pos(path, 0, 0), "some")
    val wrongTwo = JSymbol(List("MyType", "dummy2", "to", "path"), Pos(path, 0, 0), "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, symbol, wrongTwo)
    val chosen = jd.choose("Pos", Pos(path, 6, 12), choices)
    assert(chosen === Some(symbol))
  }


  "jump decider" should "handle renamed imports correctly" in {
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
    val (jd, path) = getJDForContent(content)
    val wrongOne = JSymbol(List("MyType", "link", "to", "path"), Pos(path, 0, 0), "some")
    val symbol = JSymbol(List("JSymbol", "parser", "jump", "pankaj", "com"), Pos(path, 0, 0), "some")
    val wrongWithRenamedName = JSymbol(List("Renamed", "parser", "jump", "pankaj", "com"), Pos(path, 0, 0), "some")
    val wrongTwo = JSymbol(List("MyType", "dummy2", "to", "path"), Pos(path, 0, 0), "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, symbol, wrongWithRenamedName, wrongTwo)
    val chosen = jd.choose("Renamed", Pos(path, 8, 12), choices)
    assert(chosen === Some(symbol))
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
    val correct = JSymbol(List("MyType", "pkg", "my", "com"), Pos(path, 0, 0), "some")
    val wrongOne = JSymbol(List("Some", "other", "path"), Pos(path, 0, 0), "some")
    val wrongTwo = JSymbol(List("MyType", "dummy2", "to", "path"), Pos(path, 0, 0), "some")

    // wedging in the middle to avoid being picked up because of head or tail
    val choices = List(wrongOne, correct, wrongTwo)
    val chosen = jd.choose("MyType", Pos(path, 7, 12), choices)
    assert(chosen === Some(correct))
  }
}
