package com.pankaj.jump.ident

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.pankaj.jump._
import com.pankaj.jump.db._
import java.io.{File, PrintWriter}


class IdentSearcherSpec extends FlatSpec with Matchers with MockitoSugar {
  def getFileWithContent(content: String): File = {
    val temp = File.createTempFile("temp",".scala");
    temp.deleteOnExit()
    val pw = new PrintWriter(temp)
    pw.print(content.stripMargin)
    pw.close()
    temp
  }

  def testSearch(content: String, ident: String, expected: List[(Int, Int)]) = {
    val file = getFileWithContent(content)
    val table = mock[InvertedIdentIndex]
    val is = new IdentSearcher(table)
    val matches = is.searchIdentInFile(file, ident)
    assert(expected === matches)
  }

  "parser" should "search correctly when identifier at beginning of file" in {
    testSearch(
      """val identA = identifierB.func""",
      "identA",
      List((1,5))
    )
  }

  "parser" should "search correctly when identifier not in first line" in {
    testSearch(
      "val x = 0\nval identA = identifierB.func",
      "identA",
      List((2,5))
    )
  }

  "parser" should "search correctly when more than one identifiers on same line" in {
    testSearch(
      "val identA = (identB.y, identB.z)",
      "identB",
      List(
        (1,15),
        (1,25)
      )
    )
  }

  "parser" should "search correctly when more than one identifiers, on multiple lines " in {
    testSearch(
      "val identB = (3, 4)\nval identA = (identB.y, identB.z)",
      "identB",
      List(
        (1, 5),
        (2,15),
        (2,25)
      )
    )
  }
}
