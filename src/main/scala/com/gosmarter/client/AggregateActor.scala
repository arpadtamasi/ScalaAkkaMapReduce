package com.gosmarter.client

import akka.actor.Actor
import scala.io.Source
import java.io.File
import akka.actor.ActorRef
import scala.collection.mutable.{
  Map,
  HashMap,
  SynchronizedMap
}

class AggregateActor extends Actor {

  var finalReducedMap: Map[String, Int] = new HashMap[String, Int] with SynchronizedMap[String, Int]

  def receive = {
    case "DISPLAY_LIST" => {
      println(finalReducedMap.toString())
      sender ! finalReducedMap
    }
    case x :Map[String, Int] => {
      println("AggregateActor****" + x)
      aggregateInMemoryReduce(x)
    }
  }

  def aggregateInMemoryReduce(reducedList: Map[String, Int]) = {

    reducedList.foreach(mapVal => {
      if (finalReducedMap.contains(mapVal._1)) {
        val count: Int = reducedList(mapVal._1) + finalReducedMap(mapVal._1)
        finalReducedMap(mapVal._1) = count
      } else {
        finalReducedMap.put(mapVal._1, reducedList(mapVal._1))
      }
    })
  }
}