package com.pankaj.jump.fs

import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers


class DiskCrawlerSpec extends FlatSpec with Matchers {

  "hasExtension" should "return true when one extn and it matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.java", Array("java"))
    assert(result == true)
  }

  "hasExtension" should "return false when one extn and it does not match but same size" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.lava", Array("java"))
    assert(result == false)
  }

  "hasExtension" should "return false when one extn and it does not match but different size" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.jlava", Array("java"))
    assert(result == false)
  }

  "hasExtension" should "return false when one extn that matches in the end but not same size" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.ljava", Array("java"))
    assert(result == false)
  }

  "hasExtension" should "return true when two extensions and first matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.java", Array("java", "scala"))
    assert(result == true)
  }

  "hasExtension" should "return true when two extensions and second matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.scala", Array("java", "scala"))
    assert(result == true)
  }

  "hasExtension" should "return false when two extensions and none matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.other", Array("java", "scala"))
    assert(result == false)
  }

  "hasExtension" should "return true when many extensions and one matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.java", Array("py", "php", "thrift", "java", "scala"))
    assert(result == true)
  }

  "hasExtension" should "return false when many extensions and none matches" in {
    // def hasExtension(fileName: String, extensions: Array[String]): Boolean = {
    val result = DiskCrawler.hasExtension("filename.other", Array("py", "php", "thrift", "java", "scala"))
    assert(result == false)
  }
}
