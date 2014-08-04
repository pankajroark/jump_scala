package com.pankaj.jump.db

import resource._
import java.sql.ResultSet

trait Table {
  val db: Db
  val name: String
  val createString: String
  type IndexName = String
  val indexInfo: Map[IndexName, List[String]] = Map()

  def setUp() = {
    ensureExists()
    ensureIndicesExist()
  }

  def ensureExists() = {
    if (!db.tables.contains(name)) {
      update(createString)
      println(s"$name table created")
    } else {
      println(s"$name table already exists")
    }
  }

  def existingIndices(): List[String] = {
    val rs = db.metadata.getIndexInfo(null, null, name, false, false)
    var indices = List[String]()
    while(rs.next()) {
      indices = rs.getString("INDEX_NAME") :: indices
    }
    indices
  }

  def ensureIndicesExist() {
    val curIndices = existingIndices()
    val tableIndexInfo = for((idxName, cols) <- indexInfo ) yield {
      (name + "_" + idxName, cols)
    }
    for ( (indexName, cols) <- tableIndexInfo) {
      if (!curIndices.contains(indexName)) {
        val colsStr = cols.mkString("(", ",", ")")
        val createStr = s"create index $indexName on $name $colsStr"
        update(createStr)
        println(s"create index $indexName on table $name")
      } else {
        println(s"index $indexName already exists on table $name")
      }
    }
  }

  def quote(s: String): String = "'" + s + "'"

  val identity: ResultSet => ResultSet = x => x

  // only read from rs, don't mutate, e.g. don't do next on it
  def query[A](q: String)(f: ResultSet => A): List[A] = {
    for (stmt <- managed(db.conn.createStatement)) {
      val rs = stmt.executeQuery(q)
      var result = List[A]()
      while(rs.next()) {
        result = f(rs) :: result
      }
      return result.reverse
    }
    Nil
  }

  def queryHasResults(q: String): Boolean = {
    (query(q)(identity)) match {
      case Nil => false
      case _ => true
    }
  }

  // Only use result set to read values, don't mutate
  def queryStream[A](q: String)(f: ResultSet => A): Stream[A] = {
    val stmt = db.conn.createStatement
    val rs = stmt.executeQuery(q)
    (new Iterator[A] {
      def hasNext = rs.next()
      def next() = f(rs)
    }).toStream
  }

  def update(stmtString: String) = {
    for (stmt <- managed(db.conn.createStatement)) {
      stmt.executeUpdate(stmtString)
    }
  }
}

