package com.pankaj.jump.db

import java.sql.{DatabaseMetaData, DriverManager}

// Manages the jdbc connection to database
class Db {
  Class.forName("org.h2.Driver")

  val name = "jump"

  val conn = DriverManager.getConnection(s"jdbc:h2:~/$name;MV_STORE=FALSE;MVCC=FALSE", "sa", "")

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
