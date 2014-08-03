package com.pankaj.jump

import com.twitter.util.{Await, Future, Try, Return, Throw}
import com.pankaj.jump.fs.RootsTracker
import com.pankaj.jump.util.ThreadActor

import java.net.URLDecoder
import java.net.ServerSocket
import java.net.InetSocketAddress
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.PrintWriter

class AltJumpService(
  rootsTracker: RootsTracker,
  jumpHandler: JumpHandler,
  findHandler: FindHandler,
  parseWorker: ThreadActor[Path],
  diskCrawler: ThreadActor[Unit],
  port: Int
) {

  def start() = {
    val serverSocket = new ServerSocket
    serverSocket.bind(new InetSocketAddress(port))

    val thread = new Thread(new Runnable {
      def run() = {
        while(!serverSocket.isClosed) {
          try {
            val socket = serverSocket.accept()
            socket.setSoTimeout(5000)
            val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
            val line = reader.readLine()
            val Array(method, uri, version) = line.split(' ')
            val response = handler(uri)
            val pw = new PrintWriter(socket.getOutputStream);
            response match {
              case Left(status) =>
                pw.print(genHttpResponse(status))
              case Right(msg) =>
                println(genHttpResponse(200, msg))
                pw.print(genHttpResponse(200, msg))
            }
            pw.flush()
            pw.close()
            reader.close()
            socket.close()
          } catch {
            case e: Throwable =>
              e.printStackTrace
              println("ate exception")
          }
        }
      }
    })
    thread.setDaemon(true)
    thread.setName("Server Thread")
    thread.start();
  }

  def genHttpResponse(status: Int, msg: String = ""): String = {
    status match {
      case 200 =>
        s"HTTP/1.1 200 OK\r\n\r\n$msg"
      case 404 =>
        s"HTTP/1.1 404 Not Found\r\n"
      case _ =>
        s"HTTP/1.1 500 Internal Server Error\r\n"
    }
  }

  def handler(uri: String): Either[Int, String] = {
    println(s"AltJumpService:: uri :: $uri")
    val (path, params) = parseUri(uri)
    path match {
      case "/add_root" =>
        params.get("root") match {
          case Some(rootPath) =>
            // todo return 400 in case root could not be added
            val rootAdded = rootsTracker.track(rootPath)
            if (rootAdded) {
              diskCrawler.send(())
              Right("root added")
            } else {
              Left(500)
            }
          case _ => Left(400)
        }
      case "/roots" =>
        Right(rootsTracker.roots.mkString("\n"))

      case "/jump" =>
        val result = for {
          symbol <- params.get("symbol").toTry(new Exception("symbol not supplied"))
          file <- params.get("file").toTry(new Exception("file not supplied"))
          row <- params.get("row").toTry(new Exception("row not supplied")) map (_.toInt)
          col <- params.get("col").toTry(new Exception("col not supplied")) map (_.toInt)
          jumpResult <- jumpHandler.jump(symbol, file, row, col)
        } yield {
          println(s"jump result :: $jumpResult")
          jumpResult
        }
        tryToEither(result)

      case "/find" =>
        val result = for {
          symbol <- params.get("symbol").toTry(new Exception("symbol not supplied"))
          file <- params.get("file").toTry(new Exception("file not supplied"))
          row <- params.get("row").toTry(new Exception("row not supplied")) map (_.toInt)
          col <- params.get("col").toTry(new Exception("col not supplied")) map (_.toInt)
          findResult <- findHandler.find(symbol, file, row, col)
        } yield {
          println(s"find result :: $findResult")
          findResult
        }
        tryToEither(result)

      case "/crawl" =>
        diskCrawler.send(())
        Right("crawl queued")
      case "/dirty" =>
        val result = for {
          file <- params.get("file").toTry(new Exception("file not supplied"))
        } yield {
          parseWorker.send(file)
          "marked dirty"
        }
        tryToEither(result)
      case _ => Left(404)
    }
  }

  def tryToEither(t: Try[String]): Either[Int, String] = t match {
    case Return(result) => Right(result)
    case Throw(th) =>
      th.printStackTrace
      Left(500)
  }

  def parseUri(uri: String): (String, Map[String, String]) = {
    uri.split('?') match {
      case Array(path) => (path, Map.empty[String, String])
      case Array(path, query) =>
        val queryMap = (for (q <- query.split('&')) yield {
          q.split("=") match {
            case Array(x) => (x, "")
            case Array(x, y) => (x, y)
          }
        }).toMap
        (path, queryMap)
    }
  }

}

