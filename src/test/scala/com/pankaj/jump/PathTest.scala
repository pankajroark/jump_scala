package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers


class PathSpec extends FlatSpec with Matchers {

  "path" should "build from string correctly" in {
    val path: Path = "/root/sub/file.scala"
    assert(path.parts === Seq("root", "sub", "file.scala"))
  }

  "path" should "throw if built from incorrect path string" in {
    intercept[IllegalArgumentException] {
      val path: Path = "root/sub/file.scala"
    }
  }

  "path" should "list all dirs correctly when is file" in {
    val path: Path = "/root/sub/file.scala"
    val expected: Seq[Path] = Seq("/sub", "/root/sub")
    assert(path.allDirs === expected)
  }

  "path" should "list all dirs correctly when is dir" in {
    val path: Path = "/root/sub"
    val expected: Seq[Path] = Seq("/sub", "/root/sub")
    assert(path.allDirs === expected)
  }

  "path" should "list all dirs correctly when is dir and trailing slash" in {
    val path: Path = "/root/sub/"
    val expected: Seq[Path] = Seq("/sub", "/root/sub")
    assert(path.allDirs === expected)
  }

  "path" should "convert to string correctly when is file" in {
    val path: Path = "/root/sub/file.json"
    assert("/root/sub/file.json" === path.toString)
  }

  "path" should "prepend dir correctly" in {
    val path: Path = "/root/sub/file.json"
    val expected: Path = "/new_root/root/sub/file.json"
    assert( expected === path.prependDir("new_root"))
  }

}
