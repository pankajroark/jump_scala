package com.pankaj.jump.db

import com.pankaj.jump.{Pos, Path, hash}
import com.pankaj.jump.parser.{JSymbol, PosShort, JSymbolShort}
import scala.collection.mutable
import com.google.common.hash.{BloomFilter, Funnels}
import com.google.common.base.Charsets
import java.sql.ResultSet
import java.io._

object InvertedIdentIndexInMemory {
  val StringFunnel = Funnels.stringFunnel(Charsets.UTF_8)
  // write to output stream
  // closing the stream is left to called
  def serialize(index: InvertedIdentIndexInMemory, out: OutputStream) = {
    val oos = new ObjectOutputStream(out)
    val bloomMap = index.bloomMap
    oos.writeInt(bloomMap.size)
    for ( (filename, (ts, bloom)) <- bloomMap) {
      oos.writeUTF(filename)
      oos.writeLong(ts)
      oos.writeObject(bloom)
    }
    oos.flush()
  }

  def deserialize(in: FileInputStream): InvertedIdentIndexInMemory = {
    val index = new InvertedIdentIndexInMemory
    val ois = new ObjectInputStream(in)
    var bloomMap = Map[String, ( Long, BloomFilter[CharSequence])]()
    val size = ois.readInt
    for(i <- 0 until size) {
      val filename = ois.readUTF
      val ts = ois.readLong
      val bloom = ois.readObject.asInstanceOf[BloomFilter[CharSequence]]
      bloomMap = bloomMap + (filename -> (ts -> bloom))
    }
    index._bloomMap = bloomMap
    index
  }
}

class InvertedIdentIndexInMemory extends InvertedIdentIndex {
  import InvertedIdentIndexInMemory._

  private var _bloomMap = Map[String, ( Long, BloomFilter[CharSequence])]()

  def bloomMap: Iterator[(String, ( Long, BloomFilter[CharSequence]))] =
    _bloomMap.iterator

  def addFileIdents(file: String, idents: Set[String], timestamp: Long) = {
    val bloomFilter = BloomFilter.create(StringFunnel, 1000)
    for (ident <- idents) {
      bloomFilter.put(ident)
    }
    this.synchronized {
      _bloomMap = _bloomMap + (file -> (timestamp -> bloomFilter))
    }
  }

  def filesForIdent(ident: String): List[Path] = {
    val iter = for{
      (file, (_, bloom)) <- _bloomMap
      if bloom.mightContain(ident)
    } yield Path.fromString(file)
    iter.toList
  }
}

