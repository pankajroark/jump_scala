package com.pankaj.jump

import com.twitter.util.{Await, Future, Return}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

import com.pankaj.jump.db._
import com.pankaj.jump.parser._
import com.pankaj.jump.fs._
import com.pankaj.jump.util._


class JumpServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  /*
  "the service" should "return 200 on valid request" in {
    val rootsTracker = mock[RootsTracker]
    val jumpHandler = mock[JumpHandler]
    when(jumpHandler.jump(anyString(), anyString(), anyInt(), anyInt()))
      .thenReturn(Return("/path/to/another/file:1:1"))
    val parseWorker = new ThreadNonBlockingActor({file: Path => ()})
    val diskCrawler = new ThreadNonBlockingActor({u: Unit => ()})
    val server = Http.serve(":8081", new JumpService(rootsTracker, jumpHandler, parseWorker, diskCrawler))
    try{
      val client = Http.newService("localhost:8081")
      val file = "/path/to/file"
      val url = s"/jump?file=$file&symbol=sym&row=10&col=10"
      val request =  new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.POST, url)
      val response = Await.result(client(request))
      val status = response.getStatus
      assert(HttpResponseStatus.OK === status)
    } finally {
      Await.result(server.close())
    }
  }
  */
}
