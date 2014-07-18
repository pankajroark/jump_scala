package com.pankaj.jump.db

import com.pankaj.jump.Path
import com.pankaj.jump.parser.JSymbol

// todo set up indices for name and filepath
class SymbolTable(val db: Db) extends Table {
  val name = "SYMBOL_TABLE"

  //Unique Id | name | qualified name | filepath | type | row | col
  val createString = s"create table $name(" +
    "Id int not null AUTO_INCREMENT PRIMARY KEY, " +
    "Name varchar(1024) not null, " +
    "QualName varchar(1024) not null, " +
    "FilePath varchar(1024) not null, " +
    "Type varchar(64) not null, " +
    "Row int not null, " +
    "Col int not null" +
    ")"

  // todo implement this
  def addSymbol(s: JSymbol) = {
    val symName = quote(s.name)
    val qName = quote(s.qualName)
    val file = quote(s.loc.file.toString)
    val typ = quote(s.typ)
    val row = s.loc.row
    val col = s.loc.col
    update(s"insert into $name values(" +
      s"null, $symName, $qName, $file, $typ, $row, $col" +
      ")"
    )
  }

  def printAll() {
    query(s"select * from $name") { rs =>
      println(rs)
    }
  }

}
