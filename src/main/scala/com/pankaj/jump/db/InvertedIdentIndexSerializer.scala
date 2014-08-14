package com.pankaj.jump.db

import java.io.{File, FileOutputStream, FileInputStream}

object InvertedIdentIndexSerializer {
  val homeDir = System.getProperty("user.home")
  val indexFileName = s"$homeDir/.jumpFindIndex"
}


class InvertedIdentIndexSerializer
extends (InvertedIdentIndex => Unit) {
  import InvertedIdentIndexSerializer._

  def apply(index: InvertedIdentIndex): Unit = {
    // write to a temporary file first
    // them replace the existing file with it
    val temp = File.createTempFile("temp",".index");
    val fos = new FileOutputStream(temp)
    InvertedIdentIndexInMemory.serialize(index, fos)
    fos.close
    val indexFile = new File(indexFileName)
    indexFile.delete()
    temp.renameTo(indexFile)
    println(s"written bloom index $indexFileName")
  }
}

