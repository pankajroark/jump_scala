package com.pankaj.jump.db

import resource._
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.Path

class RootsTable(db: Db) {
  val name = "ROOTS_TABLE"

  def ensureExists() {
    if (!db.tables.contains(name)) {
      val createString = s"create table $name(" +
        "Path varchar(1024) not null PRIMARY KEY, " +
        "CreationTime bigint not null" +
        ")"

      for (stmt <- managed(db.conn.createStatement)) {
        stmt.executeUpdate(createString)
      }
      println(s"$name table created")
    } else {
      println(s"$name table already exists")
    }
  }

  def quote(s: String): String = "'" + s + "'"


  def addRoot(path: Path) = {
    for (stmt <- managed(db.conn.createStatement)) {
      val root = quote(path.toString)
      val ts = System.currentTimeMillis
      val stmtStr = s"insert into $name values($root, $ts)"
      stmt.executeUpdate(stmtStr)
    }
  }

  def getRoots(): List[String] = {
    for (stmt <- managed(db.conn.createStatement)) {
      val stmtStr = s"SELECT Path FROM $name"
      val rs = stmt.executeQuery(stmtStr)
      var roots = List[String]()
      while(rs.next()) {
        roots = rs.getString(1) :: roots
      }
      return roots
    }
    Nil
  }

  // mainly for debugging
  def printRoots() {
    getRoots().map(println _)
  }
}
