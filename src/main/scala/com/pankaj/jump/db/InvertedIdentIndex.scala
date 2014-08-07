package com.pankaj.jump.db

import com.pankaj.jump.{Pos, Path, hash}
import com.pankaj.jump.parser.{JSymbol, PosShort, JSymbolShort}
import java.sql.ResultSet
import scala.collection.mutable
import com.google.common.hash.{BloomFilter, Funnels}
import com.google.common.base.Charsets

class InvertedIdentIndex {
  var bloomMap = Map[String, ( Long, BloomFilter[CharSequence])]()
  val StringFunnel = Funnels.stringFunnel(Charsets.UTF_8)

  def addFileIdents(file: String, idents: Set[String], timestamp: Long) = {
    val bloomFilter = BloomFilter.create(StringFunnel, 1000)
    for (ident <- idents) {
      bloomFilter.put(ident)
    }
    this.synchronized {
      bloomMap = bloomMap + (file -> (timestamp -> bloomFilter))
    }
  }

  def filesForIdent(ident: String): List[Path] = {
    val iter = for{
      (file, (_, bloom)) <- bloomMap
      if bloom.mightContain(ident)
    } yield Path.fromString(file)
    iter.toList
  }
}

