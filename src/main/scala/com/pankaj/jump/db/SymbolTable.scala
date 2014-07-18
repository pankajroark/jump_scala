package com.pankaj.jump.db

import com.pankaj.jump.Path

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

}

