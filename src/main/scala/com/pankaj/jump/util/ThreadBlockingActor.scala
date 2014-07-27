package com.pankaj.jump.util

import java.util.concurrent.ArrayBlockingQueue

trait ThreadActor[A] {
  def send(msg: A)
  def start()
}

// Thread actor is a simple queue based actor that executes
// messages on the queue until done
class ThreadBlockingActor[A] (
  processor: A => Unit, size: Int = 100
) extends ThreadActor[A] {
  private val queue = new ArrayBlockingQueue[A](size)
  private val loop = new Runnable {
    def run() = {
      while(true) {
        val msg = queue.take()
        processor(msg)
      }
    }
  }

  private val thread = new Thread(loop)

  // Note that this blocks
  def send(msg: A) = {
    queue.put(msg)
  }

  def start() {
    thread.start()
  }
}
