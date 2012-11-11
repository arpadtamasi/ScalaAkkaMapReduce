package com.gosmarter.client

import akka.actor.Actor
import scala.io.Source
import java.io.File

class LineReadActor extends Actor {

  def receive = {
    case x : List[String] => {

      for (line <- x) {
        sender ! line
      }
      sender ! "EOF"
    }
  }
}