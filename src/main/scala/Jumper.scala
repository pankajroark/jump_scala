import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import com.twitter.util.ScheduledThreadPoolTimer
import com.twitter.conversions.time._
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import org.jboss.netty.handler.codec.http._
import com.pankaj.jump.{JumpDecider, JumpHandler, JumpService, Path}
import com.pankaj.jump.parser.{Parser, ParseWorker}
import com.pankaj.jump.fs.{DirtFinder, DiskCrawler, RootsTracker}
import com.pankaj.jump.db.{Db, FileTable, RootsTable, SymbolTable}
import com.pankaj.jump.util.ThreadActor

object Jumper {
  def main(args:Array[String]): Unit = {
    val parser = new Parser
    val db = new Db
    val fileTable = new FileTable(db)
    val rootsTable = new RootsTable(db)
    val symbolTable = new SymbolTable(db, fileTable)
    fileTable.setUp()
    rootsTable.setUp()
    symbolTable.setUp()
    val rootsTracker = new RootsTracker(rootsTable)
    val parseWorkerActor = new ThreadActor(new ParseWorker(fileTable, symbolTable, parser))
    parseWorkerActor.start()
    val dirtFinderActor = new ThreadActor(new DirtFinder(fileTable, parseWorkerActor))
    dirtFinderActor.start()
    val diskCrawlerActor = new ThreadActor(
      new DiskCrawler(rootsTracker, fileTable, dirtFinderActor)
    )
    diskCrawlerActor.start()

    val timer = new ScheduledThreadPoolTimer()
    // todo add a command line option for this
    timer.schedule(1.minutes) {
      try {
        diskCrawlerActor.send(())
        //fileTable.printFiles()
        //rootsTable.printRoots()
        dirtFinderActor.send(())
        //symbolTable.printAll()
      } catch {
        case e: Throwable =>
          println("error")
          e.printStackTrace
      }
    }

    val jumpDecider = new JumpDecider(parser, symbolTable, fileTable)
    val jumpHandler = new JumpHandler(jumpDecider, symbolTable)
    val jumpService = new JumpService(
      rootsTracker,
      jumpHandler,
      parseWorkerActor,
      diskCrawlerActor
    )
    val server = Http.serve(":8081", jumpService)

    /*
    val file: Path = "/Users/pankajg/workspace/bc3/finagle/finagle-core/src/main/scala/com/twitter/finagle/Context.scala"
    println(Parser.parse(file).mkString("\n"))
    */
    Await.ready(server)
  }
}
