import com.twitter.util.{Await, Future}
import com.twitter.util.ScheduledThreadPoolTimer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import com.pankaj.jump.scheduleWithPeriod
import com.pankaj.jump.{AltJumpService, JumpDecider, JumpHandler, FindHandler, Path}
import com.pankaj.jump.parser.{Parser, ParserFactory, ParseWorker}
import com.pankaj.jump.fs.{DirtFinder, DiskCrawler, RootsTracker}
import com.pankaj.jump.db._
import com.pankaj.jump.util.{ThreadNonBlockingActor, ThreadBlockingActor}
import com.pankaj.jump.ident.{IdentRecorder, IdentSearcher}
import java.io.{File, FileInputStream}

object Jumper {
  def main(args:Array[String]): Unit = {
    // tables
    val db = new Db
    val fileTable = new FileTable(db)
    val rootsTable = new RootsTable(db)
    val symbolTable = new SymbolTable(db, fileTable)
    val identTable = new IdentTable(db)
    val identIndex: InvertedIdentIndexInMemory = {
      val indexFileName = InvertedIdentIndexSerializer.indexFileName
      val indexFile = new File(indexFileName)
      if (indexFile.exists) {
        println(s"reading bloom index $indexFileName")
        val is = new FileInputStream(indexFile)
        val index = InvertedIdentIndexInMemory.deserialize(is)
        is.close()
        index
      } else {
        new InvertedIdentIndexInMemory
      }
    }
    fileTable.setUp()
    rootsTable.setUp()
    symbolTable.setUp()
    identTable.setUp()

    // workers
    val parserFactory = new ParserFactory
    val rootsTracker = new RootsTracker(rootsTable)
    val identRecorder = new IdentRecorder(identTable, identIndex)
    val parseWorkerActor = new ThreadBlockingActor(new ParseWorker(fileTable, symbolTable, parserFactory, identRecorder), 10)
    parseWorkerActor.start()
    val dirtFinderActor = new ThreadNonBlockingActor(new DirtFinder(fileTable, parseWorkerActor))
    dirtFinderActor.start()
    dirtFinderActor.send(())
    val diskCrawlerActor = new ThreadNonBlockingActor(
      new DiskCrawler(rootsTracker, fileTable, dirtFinderActor)
    )
    diskCrawlerActor.start()
    val indexWriterActor = new ThreadNonBlockingActor(new InvertedIdentIndexSerializer)
    indexWriterActor.start()

    val timer = new ScheduledThreadPoolTimer()
    scheduleWithPeriod(timer, "DISK_CRAWL_PERIOD", 60){
      diskCrawlerActor.send(())
      dirtFinderActor.send(())
    }

    scheduleWithPeriod(timer, "BLOOM_SYNC_PERIOD", 300){
      indexWriterActor.send(identIndex)
    }
    val jumpDecider = new JumpDecider(parserFactory, symbolTable, fileTable, rootsTracker)
    val jumpHandler = new JumpHandler(jumpDecider, symbolTable)
    val identSearcher = new IdentSearcher(identIndex)
    val findHandler = new FindHandler(rootsTable, identSearcher)
    val port_env = System.getenv("PORT")
    val port = if (port_env != null) port_env.toInt else 8081

    val altJumpService = new AltJumpService(
      rootsTracker,
      jumpHandler,
      findHandler,
      parseWorkerActor,
      diskCrawlerActor,
      8081
    )
    altJumpService.start()
    diskCrawlerActor.send(())
  }
}
