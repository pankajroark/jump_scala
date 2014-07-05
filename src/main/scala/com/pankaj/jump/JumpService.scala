package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers

import java.net.URLDecoder

class JumpService extends Service[HttpRequest, HttpResponse] {
  val UTF8 = "UTF-8"

  def apply(req: HttpRequest): Future[HttpResponse] = {
    println(req)

    val content = URLDecoder.decode(req.getContent().toString(UTF8), UTF8)
    println("content >> " + content)

    val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    resp.setContent(ChannelBuffers.copiedBuffer(content, UTF8))

    Future.value(resp);
  }
}
