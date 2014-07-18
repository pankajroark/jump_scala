package com.pankaj.jump.db

import java.sql.DriverManager

// Manages the jdbc connection to database
class Db {
  Class.forName("org.h2.Driver")

  val name = "jump"

  val conn = DriverManager.getConnection(s"jdbc:h2:~/$name", "sa", "")

  lazy val tables = {
    var ts = List[String]()
    val meta = conn.getMetaData()
    val result = meta.getTables(null, null, null, null)
    while(result.next()) {
      ts = result.getString(3) :: ts
    }
    ts
  }
}
