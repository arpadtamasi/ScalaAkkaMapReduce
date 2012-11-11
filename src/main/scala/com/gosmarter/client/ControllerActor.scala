package com.gosmarter.client

import akka.actor.Actor
import scala.io.Source
import java.io.File
import akka.actor.ActorRef

class ControllerActor(aggregateActor: ActorRef, mapRouter: ActorRef) extends Actor {

  def receive = {
    case "DISPLAY_LIST" => {
      println("ControllerActor********")
      aggregateActor.tell("DISPLAY_LIST", sender)
      sender ! "DISPLAYED"
    }
    case x: String => mapRouter ! x
  }
}