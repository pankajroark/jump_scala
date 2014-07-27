package com.pankaj.jump.util

import java.util.concurrent.ConcurrentLinkedQueue

// Thread actor is a simple queue based actor that executes
// messages on the queue until done
class ThreadNonBlockingActor[A] (
  processor: A => Unit,
  idleSleepMillis: Int = 1000
) extends ThreadActor[A] {
  private val queue = new ConcurrentLinkedQueue[A]
  private val loop = new Runnable {
    def run() = {
      while(true) {
        val msg = queue.poll()
        if(msg == null) {
          Thread.sleep(idleSleepMillis)
        } else {
          processor(msg)
        }
      }
    }
  }

  private val thread = new Thread(loop)

  def send(msg: A) = {
    queue.add(msg)
  }

  def start() {
    thread.start()
  }
}
