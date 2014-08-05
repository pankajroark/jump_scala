package com.pankaj.jump.db

import com.pankaj.jump.{Path, hash}
import com.pankaj.jump.parser.{Pos, JSymbol, PosShort, JSymbolShort}
import java.sql.ResultSet
import scala.collection.mutable
import com.google.common.hash.{BloomFilter, Funnels}
import com.google.common.base.Charsets

class InvertedIdentIndexTable(
  val db: Db,
  identTable: IdentTable,
  fileTable: FileTable
) extends Table {
  val name = "INV_IDENT_INDEX_TABLE"
  val bloomMap = mutable.HashMap[String, BloomFilter[CharSequence]]()

  //Unique Id | name | qualified name | filepath | type | row | col
  val createString = s"create table $name(" +
    "Ident varchar(1024) not null, " +
    "FileId bigint not null, " +
    "Primary Key (FileId, Ident)" +
    ")"

  override val indexInfo = Map(
    "FILE_ID_INDEX" -> List("FileId"),
    "IDENT_ID_INDEX" -> List("Ident")
  )

  def addFileIdent(file: String, ident: String) = {
    val fileNameHash = hash(file)
    update(s"insert into $name values(${quote(ident)}, $fileNameHash)")
  }

  def addFileIdentsOld(file: String, idents: Set[String])= {
    val fileNameHash = hash(file)
    val stmt = db.conn.prepareStatement("insert into " + name + " values(?, ?)")
    try {
      for (ident <- idents) yield {
        stmt.setString(1, ident)
        stmt.setLong(2, fileNameHash)
        stmt.addBatch
      }
      stmt.executeBatch
    } finally {
      stmt.close
    }
  }

  def addFileIdents(file: String, idents: Set[String])= {
    val bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 1000)
    for (ident <- idents) {
      bloomFilter.put(ident)
    }
    bloomMap += file -> bloomFilter
  }

  def filesForIdentOld(ident: String): List[Path] = {
    val fileIds = query(s"select FileId from $name where Ident=${quote(ident)}") { rs =>
      rs.getLong("FileId")
    }
    for {
      fileId <- fileIds
      path <- fileTable.fileForId(fileId)
    } yield path
  }

  def filesForIdent(ident: String): List[Path] = {
    val iter = for{
      (file, bloom) <- bloomMap
      if bloom.mightContain(ident)
    } yield Path.fromString(file)
    iter.toList
  }
}

