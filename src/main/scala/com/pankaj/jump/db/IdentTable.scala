package com.pankaj.jump.db

import com.pankaj.jump.Path
import com.pankaj.jump.parser.{Pos, JSymbol, PosShort, JSymbolShort}
import java.sql.ResultSet

class IdentTable(val db: Db) extends Table {
  val name = "IDENT_TABLE"

  //Unique Id | name | qualified name | filepath | type | row | col
  val createString = s"create table $name(" +
    "Id int not null AUTO_INCREMENT UNIQUE, " +
    "Name varchar(80) not null PRIMARY KEY" +
    ")"

  override val indexInfo = Map(
    "NAME_INDEX" -> List("Name")
  )

  def addIdent(s: String) = {
    update(s"insert into $name values(null, $s)")
  }

  def idForName(name: String): Option[Int] = {
    (query(s"select Id from $name where Name=${quote(name)}") { rs =>
      rs.getInt("Id")
    }).headOption
  }

  def nameForId(id: Int): Option[String] = {
    (query(s"select Name from $name where Id=$id") { rs =>
      rs.getString("Name")
    }).headOption
  }

}

