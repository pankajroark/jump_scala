import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import com.twitter.util.ScheduledThreadPoolTimer
import com.twitter.conversions.time._
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import org.jboss.netty.handler.codec.http._
import com.pankaj.jump.{JumpDecider, JumpHandler, JumpService, Path}
import com.pankaj.jump.parser.{Parser, ParseWorkerThread}
import com.pankaj.jump.fs.{DirtFinder, DiskCrawler, RootsTracker}
import com.pankaj.jump.db.{Db, FileTable, RootsTable, SymbolTable}

object Hi {
  def main(args:Array[String]): Unit = {
    val db = new Db
    val fileTable = new FileTable(db)
    val rootsTable = new RootsTable(db)
    val symbolTable = new SymbolTable(db)
    fileTable.setUp()
    rootsTable.setUp()
    symbolTable.setUp()
    val rootsTracker = new RootsTracker(rootsTable)
    val diskCrawler = new DiskCrawler(rootsTracker, fileTable)
    val dirtQueue = new ConcurrentLinkedQueue[Path]
    val dirtFinder = new DirtFinder(fileTable, dirtQueue)
    val timer = new ScheduledThreadPoolTimer()
    // todo add a command line option for this
    timer.schedule(5.seconds) {
      try {
        diskCrawler.crawl()
        //fileTable.printFiles()
        //rootsTable.printRoots()
        dirtFinder.run()
        //symbolTable.printAll()
      } catch {
        case e: Throwable =>
          println("error")
          e.printStackTrace
      }
    }

    val jumpDecider = new JumpDecider
    val jumpHandler = new JumpHandler(jumpDecider)
    val jumpService = new JumpService(rootsTracker, jumpHandler)
    val server = Http.serve(":8081", jumpService)
    val parseWorker = new Thread(new ParseWorkerThread(dirtQueue, fileTable, symbolTable))
    parseWorker.start()

    /*
    val file: Path = "/Users/pankajg/workspace/bc3/finagle/finagle-core/src/main/scala/com/twitter/finagle/Context.scala"
    println(Parser.parse(file).mkString("\n"))
    */
    Await.ready(server)
  }
}
