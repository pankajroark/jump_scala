package com.pankaj.jump.db

import com.pankaj.jump.Path
import com.pankaj.jump.parser.{Pos, JSymbol, PosShort, JSymbolShort}
import java.sql.ResultSet

class InvertedIdentIndexTable(
  val db: Db,
  identTable: IdentTable,
  fileTable: FileTable
) extends Table {
  val name = "INV_IDENT_INDEX_TABLE"

  //Unique Id | name | qualified name | filepath | type | row | col
  val createString = s"create table $name(" +
    "IdentId int not null, " +
    "FileId int not null, " +
    "Primary Key (FileId, IdentId)" +
    ")"

  override val indexInfo = Map(
    "FILE_ID_INDEX" -> List("FileId"),
    "IDENT_ID_INDEX" -> List("IdentId")
  )

  def addFileIdent(file: String, ident: String) = {
    for{
      fileId <- fileTable.idForFile(file)
      identId <- identTable.idForName(ident)
    } {
      val lookupQuery =
        s"select * from $name where IdentId=$identId and FileId=$fileId"
      if (!queryHasResults(lookupQuery)) {
        update(s"insert into $name values($identId, $fileId)")
      }
    }
  }

  def filesForIdent(identId: Int): List[Int] = {
    query(s"select FileId from $name where IdentId=$identId") { rs =>
      rs.getInt("FileId")
    }
  }

  def filesForIdent(ident: String): List[Path] = {
    for{
      identId <- identTable.idForName(ident).toList
      fileId <- filesForIdent(identId)
      file <- fileTable.fileForId(fileId)
    } yield file
  }

}

