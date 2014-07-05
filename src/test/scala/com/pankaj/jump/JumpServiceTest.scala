package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers


class JumpServiceSpec extends FlatSpec with Matchers {

  "the service" should "return 200 on valid request" in {
    val server = Http.serve(":8081", new JumpService)
    try{
      val client = Http.newService("localhost:8081")
      val request =  new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.POST, "/jump")
      val jumpLoc = "/Users/pankajg/workspace/birdcage/adservfaux/testclient/src/main/scala/com/twitter/adservfaux/service/AdServFauxTestService.scala:100,40"
      val content = ChannelBuffers.copiedBuffer(jumpLoc, "UTF-8")
      request.setContent(content)
      HttpHeaders.setContentLength(request, content.readableBytes)
      val response = Await.result(client(request))
      val status = response.getStatus
      assert(HttpResponseStatus.OK === status)
    } finally {
      Await.result(server.close())
    }
  }
}
