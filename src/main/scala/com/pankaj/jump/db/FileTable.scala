package com.pankaj.jump.db

import resource._
import com.pankaj.jump.fs.FileInfo
import com.pankaj.jump.Path

class FileTable(val db: Db) extends Table {
  val name = "FILE_TABLE"

  val createString = s"create table $name(" +
    "Path varchar(1024) not null PRIMARY KEY, " +
    "ModStamp bigint not null, " +
    "ProcessStamp int, " +
     "Imports varchar(60000)" +
    ")"

  def fileExists(file: Path): Boolean = {
    val path = quote(file.toString)
    queryHasResults(s"select * from $name where Path=$path")
  }

  def addFileWithModStamp(fileInfo: FileInfo) = {
    val file = quote(fileInfo.path.toString)
    val modStamp = fileInfo.modStamp.toString
    update(s"insert into $name (Path, ModStamp, ProcessStamp) values ($file, $modStamp, 0)")
  }

  def updateModStamp(fileInfo: FileInfo) = {
    val file = quote(fileInfo.path.toString)
    val modStamp = fileInfo.modStamp.toString
    update(s"update $name set ModStamp=$modStamp where Path=$file")
  }

  def addOrUpdateFileWithModStamp(fileInfo: FileInfo) = {
    // check if entry already exists, if so update
    // else create a new entry
    if (fileExists(fileInfo.path)) updateModStamp(fileInfo)
    else addFileWithModStamp(fileInfo)
  }

  // Returns a stream of results
  // todo figure out stream close mechanism:
  // how do we make sure the underlying result set
  // is closed?
  def allFiles: Stream[(FileInfo, Long)] = {
    queryStream(s"SELECT * FROM $name") { rs =>
      (FileInfo(rs.getString(1), rs.getLong(2)), rs.getLong(3))
    }
  }

  // mainly for debugging
  def printFiles() {
    allFiles.toList.map(println _)
  }
}
