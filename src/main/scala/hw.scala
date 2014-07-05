import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import com.pankaj.jump.JumpService

object Hi {
  def main(args:Array[String]): Unit = {
    val server = Http.serve(":8081", new JumpService)

    Await.ready(server)
  }
}
