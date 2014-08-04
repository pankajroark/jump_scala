package com.pankaj.jump.ident

import com.pankaj.jump.parser.Pos
import com.pankaj.jump.db.InvertedIdentIndexTable
import java.io.{File, BufferedReader, FileReader}
import scala.collection.mutable.ListBuffer

class IdentSearcher(invIdentIndexTable: InvertedIdentIndexTable) {
  def search(ident: String): List[Pos] = {
    val paths = invIdentIndexTable.filesForIdent(ident)
    println(s"paths :: $paths")
    // Now search in all these files and get positions
    for{
      path <- paths
      (row, col) <- searchIdentInFile(path.toFile, ident)
    } yield {
      Pos(path, row, col)
    }
  }

  def searchIdentInFile(file: File, ident: String): List[(Int, Int)] = {
    println(s" File -> $file.getPath, ident -> $ident")
    val reader = new BufferedReader(new FileReader(file))
    var line = ""
    var locations = new ListBuffer[(Int, Int)]()
    var row = 0
    while(true) {
      line = reader.readLine
      if (line == null) {
        println(locations.toList.mkString(",\n"))
        return locations.toList
      } else {
        row += 1
        var matchIndex = -2
        var fromIndex = 0
        while(matchIndex != -1) {
          matchIndex = line.indexOf(ident, fromIndex)
          // println(s" $matchIndex : $fromIndex")
          if (matchIndex != -1) {
            println(line)
            locations += ((row, matchIndex + 1))
            fromIndex = matchIndex + ident.size
          }
        }
      }
    }
    locations.toList
  }
}
