package com.gosmarter.client

import akka.actor.Actor
import scala.io.Source
import java.io.File
import akka.actor.ActorRef
import scala.collection.mutable.{
  Map,
  HashMap
}

class ReduceActor(aggregateActor: ActorRef) extends Actor {

  def receive = {
    case x: List[Result] => {
      println("ReduceActor****" + x)
      aggregateActor ! reduce(x)
    }
  }

  def reduce(list: List[Result]): Map[String, Int] = {
    var results: Map[String, Int] = new HashMap[String, Int]

    list.foreach(result => {
      if (results.contains(result.word)) {
        results(result.word) += result.noOfInstances
      } else {
        results(result.word) = result.noOfInstances
      }
    })
    return results;
  }
}