import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import com.twitter.util.ScheduledThreadPoolTimer
import com.twitter.conversions.time._
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import com.pankaj.jump.{JumpService, Path}
import com.pankaj.jump.parser.Parser
import com.pankaj.jump.fs.{DiskCrawler, RootsTracker}
import com.pankaj.jump.db.{Db, FileTable, RootsTable}

object Hi {
  def main(args:Array[String]): Unit = {
    val db = new Db
    val fileTable = new FileTable(db)
    fileTable.ensureExists()
    val rootsTable = new RootsTable(db)
    rootsTable.ensureExists()
    val rootsTracker = new RootsTracker(rootsTable)
    val diskCrawler = new DiskCrawler(rootsTracker, fileTable)
    val timer = new ScheduledThreadPoolTimer()
    // todo add a command line option for this
    timer.schedule(5.seconds) {
      try {
        diskCrawler.crawl()
        fileTable.printFiles()
        rootsTable.printRoots()
      } catch {
        case e: Throwable =>
          println("error")
          e.printStackTrace
      }
    }

    val jumpService = new JumpService(rootsTracker)
    val server = Http.serve(":8081", jumpService)
    /*
    val file: Path = "/Users/pankajg/workspace/bc3/finagle/finagle-core/src/main/scala/com/twitter/finagle/Context.scala"
    println(Parser.parse(file).mkString("\n"))
    */
    Await.ready(server)
  }
}
