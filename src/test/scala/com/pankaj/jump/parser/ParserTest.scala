package com.pankaj.jump.parser

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import com.pankaj.jump._
import java.io.{File, PrintWriter}


class ParserSpec extends FlatSpec with Matchers {

  /*
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
    val temp = File.createTempFile("temp",".scala");
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    val path: Path = Path.fromString(temp.getPath)
    val parser = new Parser
    println(parser.trackDownSymbol("choose", Pos(path, 7, 7)))
    temp.deleteOnExit()
  }
  */

  "parser" should "track down symbol correctly when wildcard imports" in {
    val content = """
    |package com
    |package pankaj.jump
    |
    |import com.pankaj.jump.parser._
    |
    |class JumpDecider(parser: Parser) {
    |  def choose(word: String, pos: Pos, choices: List[JSymbol]): Option[JSymbol] = {
    |    parser.trackDownSymbol(word, pos)
    |    choices.headOption
    |  }
    |}
    """
    val temp = File.createTempFile("temp",".scala");
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    val path: Path = Path.fromString(temp.getPath)
    val parser = new Parser
    println(parser.trackDownSymbol("choose", Pos(path, 7, 7)))
    temp.deleteOnExit()
  }
}
