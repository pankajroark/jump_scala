package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.path.{Path => FinaglePath, Root, :?, /, ParamMatcher}
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future, Try}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer

import java.net.URLDecoder

class JumpService extends Service[HttpRequest, HttpResponse] {
  val UTF8 = "UTF-8"

  def okResponse(msg: => String) = {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    response.setContent(copiedBuffer(msg, UTF8))
    response
  }

  def apply(req: HttpRequest): Future[HttpResponse] = {
    println(req)
    val request = Request(req)

    object RootMatcher extends ParamMatcher("root")

    (FinaglePath(request.path) :? request.params) match {
      case Root / "add_root" :? RootMatcher(path)=>
        Future { okResponse(path) }
      case _ => Future{ new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)}
    }

  }
}

