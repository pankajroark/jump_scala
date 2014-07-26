package com.pankaj.jump.db

import com.pankaj.jump.Path
import com.pankaj.jump.parser.{Pos, JSymbol, PosShort, JSymbolShort}
import java.sql.ResultSet

// todo set up indices for name and filepath
class SymbolTable(val db: Db, fileTable: FileTable) extends Table {
  val name = "SYMBOL_TABLE"

  //Unique Id | name | qualified name | filepath | type | row | col
  val createString = s"create table $name(" +
    "Id int not null AUTO_INCREMENT PRIMARY KEY, " +
    "Name varchar(1024) not null, " +
    "QualName varchar(1024) not null, " +
    "FileId Int not null, " +
    "Type varchar(64) not null, " +
    "Row int not null, " +
    "Col int not null" +
    ")"

  override val indexInfo = Map(
    "FILE_ID_INDEX" -> List("FileId"),
    "NAME_INDEX" -> List("Name")
  )

  def addSymbol(s: JSymbol) = {
    for (fileId <- fileTable.idForFile(s.loc.file)) {
      val symName = quote(s.name)
      val qName = quote(s.qualName)
      val typ = quote(s.typ)
      val row = s.loc.row
      val col = s.loc.col
      update(s"insert into $name values(" +
        s"null, $symName, $qName, $fileId, $typ, $row, $col" +
        ")"
      )
    }
  }

  def deleteSymbolsForFile(path: Path) =
    for (fileId <- fileTable.idForFile(path)) {
      val query = s"delete from $name where FileId=$fileId"

      println(s"delete query $query")
      update(query)
    }

  def rowToJSymbol(rs: ResultSet): JSymbolShort = {
    val qname = rs.getString("QualName")
    val rfqn = qname.split('.').reverse.toList
    val fileId = rs.getInt("FileId")
    val row = rs.getInt("Row")
    val col = rs.getInt("Col")
    val typ = rs.getString("Type")
    JSymbolShort(rfqn, PosShort(fileId, row, col), typ)
  }

  def symbolsForName(word: String): List[JSymbolShort] = {
    query(s"select * from $name where Name=${quote(word)}") { rs =>
      rowToJSymbol(rs)
    }
  }

  def printAll() {
    query(s"select * from $name") { rs =>
      println(rowToJSymbol(rs))
    }
  }

}

