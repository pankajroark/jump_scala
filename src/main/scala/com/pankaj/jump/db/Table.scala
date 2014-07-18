package com.pankaj.jump.db

import resource._
import java.sql.ResultSet

trait Table {
  val db: Db
  val name: String
  val createString: String

  def ensureExists() = {
    if (!db.tables.contains(name)) {
      for (stmt <- managed(db.conn.createStatement)) {
        stmt.executeUpdate(createString)
      }
      println(s"$name table created")
    } else {
      println(s"$name table already exists")
    }
  }

  def quote(s: String): String = "'" + s + "'"

  def query[A](q: String)(f: ResultSet => A): Option[A] = {
    for (stmt <- managed(db.conn.createStatement)) {
      val rs = stmt.executeQuery(q)
      return Some(f(rs))
    }
    None
  }

  def queryHasResults(q: String): Boolean = {
    (query(q) { _.next() }) match {
      case Some(b) => b
      case None => false
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

