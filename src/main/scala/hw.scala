import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._

object Hi {
  def main(args:Array[String]): Unit = {
    val service = new Service[HttpRequest, HttpResponse] {
      def apply(req: HttpRequest): Future[HttpResponse] =
        Future.value(new DefaultHttpResponse(
          req.getProtocolVersion, HttpResponseStatus.OK))
    }
    val server = Http.serve(":8081", service)

    val client = Http.newService("localhost:8081")
    val request =  new DefaultHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
    val response = client(request)
    response onSuccess { resp =>
      println(resp)
    }
    Await.ready(server)
  }
}
