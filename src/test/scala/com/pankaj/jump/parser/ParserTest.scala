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
  def getPathForContent(content: String, extn: String = "scala"): Path = {
    val temp = File.createTempFile("temp","." + extn);
    temp.deleteOnExit()
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    Path.fromString(temp.getPath)
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
    val temp = File.createTempFile("temp",".scala");
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    val path: Path = Path.fromString(temp.getPath)
    val parser = new Parser
    val (imps, pkgs) = parser.trackDownSymbol("choose", Pos(path, 7, 7))
    assert(imps.size === 3)
    assert(imps.head.qual === List("parser", "jump", "pankaj", "com"))
    assert(imps.head.name === "JSymbol")
    assert(imps(1).name === "Pos")
    assert(imps(1).qual === List("parser", "jump", "pankaj", "com"))
    assert(imps(2).name === "Parser")
    assert(imps(2).qual === List("parser", "jump", "pankaj", "com"))
    temp.deleteOnExit()
  }

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
    val (imps, pkgs) = parser.trackDownSymbol("choose", Pos(path, 7, 7))
    assert(pkgs === List("jump", "pankaj", "com"))
    temp.deleteOnExit()
  }

  "parser" should "collect symbols correctly" in {
    val content = """
    |package test
    |package a.b
    |
    |import d.e.f
    |
    |object Outer {
    |  class Inner(parser: Parser) {
    |    val v1 = new String
    |    type StringAlias = String
    |    def func(word: Int) = {
    |      v1.size
    |    }
    |  }
    |}
    """

    val path = getPathForContent(content)
    val parser = new Parser
    val symbols = parser.listSymbols(path)
    assert(symbols.exists(_.rfqn == List("Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("v1", "Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("func", "Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("StringAlias", "Inner", "Outer", "b", "a", "test")) == true)
  }

  "parser" should "collect symbols correctly for case classes and objects" in {
    val content = """
    |package test
    |
    |object Outer {
    | case class CC
    | case object CO
    |}
    """

    val path = getPathForContent(content)
    val parser = new Parser
    val symbols = parser.listSymbols(path)
    assert(symbols.exists(_.rfqn == List("CC", "Outer", "test")) == true)
    assert(symbols.exists(_.rfqn == List("CO", "Outer","test")) == true)
  }

  "parser" should "collect package object correctly" in {
    val content = """
    |package test.a
    |package object b {
    |
    |  import d.e.f
    |
    |  object Outer {
    |    class Inner(parser: Parser) {
    |      val v1 = new String
    |      type StringAlias = String
    |      def func(word: Int) = {
    |        v1.size
    |      }
    |    }
    |  }
    |}
    """

    val path = getPathForContent(content)
    val parser = new Parser
    val symbols = parser.listSymbols(path)
    assert(symbols.exists(_.rfqn == List("Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("v1", "Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("func", "Inner", "Outer", "b", "a", "test")) == true)
    assert(symbols.exists(_.rfqn == List("StringAlias", "Inner", "Outer", "b", "a", "test")) == true)
  }

  "parser" should "parse simple java file with a class correctly" in {
    val content = """
    |package com.twitter.ads.internal.eventstore;
    |
    |import com.twitter.ads.internal.trafficquality.InvalidTrafficDetector;
    |
    |public class AdTracker extends SharedEventProcessor {
    | private static final Logger LOG = LoggerFactory.get();
    | private List<SharedEventProcessor> eventProcessors;
    |
    | @Override
    | public void init() {
    |   for(SharedEventProcessor eventProcessor : eventProcessors) {
    |   }
    | }
    |
    |}
    """

    val path = getPathForContent(content, "java")
    val parser = new Parser
    val symbols = parser.listSymbols(path)
    println(symbols)
  }
}
