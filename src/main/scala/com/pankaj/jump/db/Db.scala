package com.pankaj.jump.db

import java.sql.{DatabaseMetaData, DriverManager}

// Manages the jdbc connection to database
class Db {
  Class.forName("org.h2.Driver")

  val name = "jump"
  val dbOptions = List(
    "MV_STORE=FALSE",
    "MVCC=FALSE"
  ).mkString(";")

  val dbString = s"jdbc:h2:~/$name;$dbOptions"
  println(dbString)

  val conn = DriverManager.getConnection(dbString, "sa", "")

  lazy val metadata: DatabaseMetaData = conn.getMetaData

  lazy val tables = {
    var ts = List[String]()
    val result = metadata.getTables(null, null, null, null)
    while(result.next()) {
      ts = result.getString(3) :: ts
    }
    ts
  }
}
