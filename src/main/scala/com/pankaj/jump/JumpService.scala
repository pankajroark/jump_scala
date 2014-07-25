package com.pankaj.jump

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.path.{Path => FinaglePath, Root, :?, /, ParamMatcher}
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future, Try}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer

import com.pankaj.jump.fs.RootsTracker
import com.pankaj.jump.util.ThreadActor

import java.net.URLDecoder

class JumpService(
  rootsTracker: RootsTracker,
  jumpHandler: JumpHandler,
  parseWorker: ThreadActor[Path],
  diskCrawler: ThreadActor[Unit]
) extends Service[HttpRequest, HttpResponse] {

  val UTF8 = "UTF-8"
  val httpVer = HttpVersion.HTTP_1_1

  val NotFound = new DefaultHttpResponse(httpVer, HttpResponseStatus.NOT_FOUND)

  def okResponse(msg: => String) = {
    val response = new DefaultHttpResponse(httpVer, HttpResponseStatus.OK)
    response.setContent(copiedBuffer(msg, UTF8))
    response
  }

  def apply(req: HttpRequest): Future[HttpResponse] = {
    println(req)
    val request = Request(req)

    object RootMatcher extends ParamMatcher("root")

    (FinaglePath(request.path)) match {
      case Root / "add_root" =>
        Future {
          request.params.get("root") match {
            case Some(path) =>
              // todo return 400 in case root could not be added
              val rootAdded = rootsTracker.track(path)
              if (rootAdded) diskCrawler.send(())
              okResponse(rootAdded.toString)
            case _ => NotFound
          }
        }

      case Root / "roots" =>
        Future {
          okResponse(rootsTracker.roots.mkString("\n"))
        }

      case Root/ "jump" =>
        val params = request.params
        Future.const(for {
          symbol <- params.get("symbol").toTry(new Exception("symbol not supplied"))
          file <- params.get("file").toTry(new Exception("file not supplied"))
          row <- params.get("row").toTry(new Exception("row not supplied")) map (_.toInt)
          col <- params.get("col").toTry(new Exception("col not supplied")) map (_.toInt)
          jumpResult <- jumpHandler.jump(symbol, file, row, col)
        } yield {
          okResponse(jumpResult)
        })

      case Root / "dirty" =>
        val params = request.params
        Future.const(for {
          file <- params.get("file").toTry(new Exception("file not supplied"))
        } yield {
          parseWorker.send(file)
          okResponse("marked dirty")
        })

      case _ => Future{NotFound}
    }

  }
}

