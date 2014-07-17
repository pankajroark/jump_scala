package com.pankaj.jump.fs

// Crawl the disk at roots and update mod stamp of files
// in File table
// Only look for java/scala files
// Need to use some kind of periodic task runner
class DiskCrawler(rootsTracker: RootsTracker) {
  def crawl() {
    println("crawl")
  }
}
