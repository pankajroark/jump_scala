package com.pankaj.jump.db

import resource._
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.Path

class RootsTable(val db: Db) extends Table {
  val name = "ROOTS_TABLE"

  val createString = s"create table $name(" +
    "Path varchar(1024) not null PRIMARY KEY, " +
    "CreationTime bigint not null" +
    ")"

  def addRoot(path: Path) = {
    val root = quote(path.toString)
    val ts = System.currentTimeMillis
    update(s"insert into $name values($root, $ts)")
  }

  def getRoots(): List[String] =
    query(s"SELECT Path FROM $name")(rs => rs.getString(1))

  // mainly for debugging
  def printRoots() {
    getRoots().map(println _)
  }
}
