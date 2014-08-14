package com.pankaj.jump.db

import com.google.common.hash.BloomFilter
import com.pankaj.jump.Path

trait InvertedIdentIndex {
  def addFileIdents(file: String, idents: Set[String], timestamp: Long)
  def filesForIdent(ident: String): List[Path]
}

