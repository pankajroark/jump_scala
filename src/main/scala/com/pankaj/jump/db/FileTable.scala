package com.pankaj.jump.db

import resource._
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.Path

class FileTable(db: Db) {
  val name = "FILE_TABLE"

  def ensureExists() {
    if (!db.tables.contains(name)) {
      val createString = s"create table $name(" +
        "Path varchar(1024) not null PRIMARY KEY, " +
        "ModStamp bigint not null, " +
        "ProcessStamp int, " +
         "Imports varchar(60000)" +
        ")"

      for (stmt <- managed(db.conn.createStatement)) {
        stmt.executeUpdate(createString)
      }
      println("file table created")
    } else {
      println("file table already exists")
    }
  }

  def fileExists(file: Path): Boolean = {
    for (stmt <- managed(db.conn.createStatement)) {
      val path = quote(file.toString)
      val stmtStr = s"select * from $name where Path=$path"
      val rs = stmt.executeQuery(stmtStr)
      return rs.next()
    }
    return false
  }

  def quote(s: String): String = "'" + s + "'"

  def addFileWithModStamp(fileInfo: FileInfo) = {
    for (stmt <- managed(db.conn.createStatement)) {
      val file = quote(fileInfo.path.toString)
      val modStamp = fileInfo.modStamp.toString
      stmt.executeUpdate(
        s"insert into $name (Path, ModStamp, ProcessStamp) values ($file, $modStamp, 0)"
      )
    }
  }

  def updateModStamp(fileInfo: FileInfo) = {
    for (stmt <- managed(db.conn.createStatement)) {
      val file = quote(fileInfo.path.toString)
      val modStamp = fileInfo.modStamp.toString
      stmt.executeUpdate(s"update $name set ModStamp=$modStamp where Path=$file")
    }
  }

  def addOrUpdateFileWithModStamp(fileInfo: FileInfo) = {
    // check if entry already exists, if so update
    // else create a new entry
    if(fileExists(fileInfo.path)) {
      updateModStamp(fileInfo)
    } else {
      addFileWithModStamp(fileInfo)
    }
  }

  // Returns a stream of results
  // todo figure out stream close mechanism:
  // how do we make sure the underlying result set
  // is closed?
  def allFiles: Stream[(FileInfo, Long)] = {
    val stmt = db.conn.createStatement
    val stmtStr = s"SELECT * FROM $name"
    val rs = stmt.executeQuery(stmtStr)
    (new Iterator[(FileInfo, Long)] {
      def hasNext = rs.next()
      def next() = (FileInfo(rs.getString(1), rs.getLong(2)), rs.getLong(3))
    }).toStream
  }

  // mainly for debugging
  def printFiles() {
    allFiles.toList.map(println _)
  }
}
