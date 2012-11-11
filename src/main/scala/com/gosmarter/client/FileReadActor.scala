package com.gosmarter.client

import akka.actor.Actor
import scala.io.Source
import java.io.File

class FileReadActor extends Actor {

  def receive = {
    case x => {

      val fileName : String = x.toString()
      println("****" + fileName)
      for (line : String <- Source.fromURL(getClass.getResource(fileName)).getLines()) {
        sender ! line
      }
      sender ! "EOF"
    }
  }
}