package com.pankaj.jump.db

import resource._
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.{Path, hash}
import java.io._
import com.google.common.base.Charsets
import com.google.common.hash.{BloomFilter, Funnels}

class InvertedIdentIndexTable(
  val db: Db,
  fileTable: FileTable
) extends Table with InvertedIdentIndex {
  val name = "FILE_TABLE"

  val StringFunnel = Funnels.stringFunnel(Charsets.UTF_8)

  val createString = s"create table $name(" +
    "FileId bigint not null PRIMARY KEY, " +
    "ProcessStamp bigint, " +
    "Bloom binary(1000) not null" +
    ")"

  def addFileIdents(file: String, idents: Set[String], timestamp: Long) = {
    val bloomFilter = BloomFilter.create(StringFunnel, 1000)
    for (ident <- idents) {
      bloomFilter.put(ident)
    }
    // write the bloom filter to table
    fileTable.idForFile(file) map { fileId =>
      val prep = db.conn.prepareStatement(
        s"insert into $name (FileId, ProcessStamp, Bloom) values (?, ?, ?)"
      )
      prep.setLong(1, fileId);
      prep.setLong(2, timestamp);
      // todo serialize the bloom filter and write
      prep.setBytes(3, bloomFilterToBytes(bloomFilter));
      prep.executeBatch()
    }
  }

  def bloomFilterToBytes(bf: BloomFilter[CharSequence]): Array[Byte] = {
    val bas = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(bas)
    oos.writeObject(bf)
    oos.flush()
    oos.close
    bas.toByteArray
  }

  def filesForIdent(ident: String): List[Path] = {
    var fileIds = List[Long]()
    query("select * from $name") { rs =>
      val is = rs.getBinaryStream("Bloom")
      val ois = new ObjectInputStream(is)
      val bloom = ois.readObject.asInstanceOf[BloomFilter[CharSequence]]
      ois.close
      if (bloom.mightContain(ident)) {
        fileIds = rs.getLong("FileId") :: fileIds
      }
    }
    for {
      fileId <- fileIds
      file <- fileTable.fileForId(fileId)
    } yield file
  }
}
