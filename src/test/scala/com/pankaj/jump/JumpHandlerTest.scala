package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import com.twitter.io.TempDirectory

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import java.io.File


class JumpHandlerSpec extends FlatSpec with Matchers {

  "findGitRoot" should "correctly find the git root" in {

    val tempDir: File = TempDirectory.create(false)
    // todo finish this
  }

}
