package com.pankaj.jump.ident

import com.pankaj.jump.{Path, Pos}
import com.pankaj.jump.db.InvertedIdentIndex
import java.io.{File, BufferedReader, FileReader}
import scala.collection.mutable.ListBuffer

class IdentSearcher(invIdentIndex: InvertedIdentIndex) {
  def search(ident: String, atPos: Pos): List[Pos] = {
    val paths = invIdentIndex.filesForIdent(ident)
    //println(s"paths :: $paths")
    // Now search in all these files and get positions
    val poss = for {
      path <- paths
      (row, col, line) <- searchIdentInFile(path.toFile, ident)
      if !unallowed(line)
    } yield Pos(path, row, col)

    val (files, specs) = poss.partition(pos => !isSpec(pos.file))
    (sorter(atPos, files) ++ specs).take(50)
    // todo lower priority for imports
    // todo closer to the current file the better
  }

  def sorter(from: Pos, poss: List[Pos]): List[Pos] = {
    // same file first priority
    val (inSameFile, rest) = poss.partition( _.file == from.file)
    val fileParent = from.file.parent
    // same directory second
    val (inSameDir, rest1) = rest.partition(fileParent == _.file.parent)
    // under same directory third
    val (under, rest2) = rest1.partition(p =>isUnder(fileParent.get, p.file))
    // todo: other last, but should be sorted by distance
    // Actually we could just keep going parent by parent and look under that, in that order
    inSameFile.sorted ++ inSameDir.sorted ++ under.sorted ++ rest2
  }

  def commonPrefix(p1: Path, p2: Path): Path = {
    val commonPartPairs = (p1.parts zip p2.parts).takeWhile(Function.tupled(_ == _))
    Path(commonPartPairs.map(_._1))
  }

  //
  def isUnder(root: Path, below: Path): Boolean = {
    commonPrefix(root, below) == root
  }

  def unallowed(line: String): Boolean = {
    val trimmed = line.trim
    trimmed.startsWith("import") ||
    trimmed.startsWith("//") ||
    trimmed.startsWith("*") ||
    trimmed.startsWith("/*")
  }

  def isSpec(path: Path): Boolean = {
    val bn = path.fileBasename
    bn.endsWith("Spec") || bn.endsWith("Test")
  }

  def searchIdentInFile(file: File, ident: String): List[(Int, Int, String)] = {
    //println(s" File -> $file.getPath, ident -> $ident")
    val reader = new BufferedReader(new FileReader(file))
    var line = ""
    var locations = new ListBuffer[(Int, Int, String)]()
    var row = 0
    while(true) {
      line = reader.readLine
      if (line == null) {
        //println(locations.toList.mkString(",\n"))
        return locations.toList
      } else {
        row += 1
        var matchIndex = -2
        var fromIndex = 0
        while(matchIndex != -1) {
          matchIndex = line.indexOf(ident, fromIndex)
          // println(s" $matchIndex : $fromIndex")
          if (matchIndex != -1) {
            //println(line)
            locations += ((row, matchIndex + 1, line))
            fromIndex = matchIndex + ident.size
          }
        }
      }
    }
    locations.toList
  }
}
