import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import com.pankaj.jump.JumpService
import com.pankaj.jump.Path
import com.pankaj.jump.parser.Parser

object Hi {
  def main(args:Array[String]): Unit = {
    val server = Http.serve(":8081", new JumpService)
    val file: Path = "/Users/pankajg/workspace/bc3/finagle/finagle-core/src/main/scala/com/twitter/finagle/Context.scala"
    println(Parser.parse(file).mkString("\n"))
    Await.ready(server)
  }
}
