package com.gosmarter.actor.test

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import akka.routing.RoundRobinRouter
import com.gosmarter.client.ControllerActor
import com.gosmarter.client.AggregateActor
import com.gosmarter.client.ReduceActor
import com.gosmarter.client.MapActor
import com.gosmarter.client.Result
import scala.collection.mutable.Map
import com.gosmarter.client.FileReadActor
import com.gosmarter.client.LineReadActor

class MapReduceSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MapReduceSpec"))

  override def afterAll {
    system.shutdown()
  }

  "Aggregrate actor" must {

    "send back Map message" in {

      // create the aggregate Actor
      val aggregateActor = system.actorOf(Props[AggregateActor]);

      var map: Map[String, Int] = Map[String, Int]("ak" -> 1, "kp" -> 2)

      aggregateActor ! map

      var map1: Map[String, Int] = Map[String, Int]("ak" -> 1, "kp" -> 2)
      aggregateActor ! map1

      Thread.sleep(1000)

      var output = Map("kp" -> 4, "ak" -> 2)
      aggregateActor ! "DISPLAY_LIST"

      expectMsg(output)
    }
  }

  "Reduce actor" must {

    "send back Map message" in {

      // create the aggregate Actor
      val aggregateActor = system.actorOf(Props[AggregateActor]);

      // create the list of reduce Actors
      val reduceRouter = system.actorOf(Props(new ReduceActor(aggregateActor)))

      val list: List[Result] = List[Result](new Result("kp", 1), new Result("ak", 2))

      reduceRouter ! list

      val list1: List[Result] = List[Result](new Result("ak", 1), new Result("kp", 2))

      reduceRouter ! list1

      Thread.sleep(1000)

      var output = Map("kp" -> 3, "ak" -> 3)
      aggregateActor ! "DISPLAY_LIST"

      expectMsg(output)
    }
  }

  "Map actor" must {

    "send back Map message" in {

      // create the aggregate Actor
      val aggregateActor = system.actorOf(Props[AggregateActor]);

      // create the list of reduce Actors
      val reduceRouter = system.actorOf(Props(new ReduceActor(aggregateActor)).withRouter(RoundRobinRouter(nrOfInstances = 2)))

      // create the list of map Actors
      val mapRouter = system.actorOf(Props(new MapActor(reduceRouter)).withRouter(RoundRobinRouter(nrOfInstances = 2)))

      var line = "Aditya Krishna Kartik Manjula"

      mapRouter ! line

      Thread.sleep(1000)

      var output = Map("Kartik" -> 1, "Krishna" -> 1, "Aditya" -> 1, "Manjula" -> 1)
      aggregateActor ! "DISPLAY_LIST"
      expectMsg(output)
    }
  }

  "List Reader Controller actor" must {

    "send back Map message" in {

      // create the aggregate Actor
      val aggregateActor = system.actorOf(Props[AggregateActor]);

      // create the list of reduce Actors
      val reduceRouter = system.actorOf(Props(new ReduceActor(aggregateActor)).withRouter(RoundRobinRouter(nrOfInstances = 2)))

      // create the list of map Actors
      val mapRouter = system.actorOf(Props(new MapActor(reduceRouter)).withRouter(RoundRobinRouter(nrOfInstances = 2)))

      val controller = system.actorOf(Props(new ControllerActor(aggregateActor, mapRouter)))

      val lineReadActor = system.actorOf(Props[LineReadActor])

      var list = List[String]("Aditya Krishna Kartik Manjula", "Manjula Anand Aditya Kartik", "Anand Vani Phani Aditya", "Kartik Krishna Manjula Aditya", "Vani Phani Anand Manjula")

      lineReadActor.tell(list, controller)

      Thread.sleep(1000)

      var output = Map("Anand" -> 3, "Kartik" -> 3, "EOF" -> 1, "Krishna" -> 2, "Vani" -> 2, "Phani" -> 2, "Aditya" -> 4, "Manjula" -> 4)
      aggregateActor ! "DISPLAY_LIST"
      expectMsg(output)
    }
  }

  "File Reader Controller actor" must {

    "send back DISPLAYED message" in {

      // create the aggregate Actor
      val aggregateActor = system.actorOf(Props[AggregateActor]);

      // create the list of reduce Actors
      val reduceRouter = system.actorOf(Props(new ReduceActor(aggregateActor)).withRouter(RoundRobinRouter(nrOfInstances = 5)))

      // create the list of map Actors
      val mapRouter = system.actorOf(Props(new MapActor(reduceRouter)).withRouter(RoundRobinRouter(nrOfInstances = 5)))

      val controller = system.actorOf(Props(new ControllerActor(aggregateActor, mapRouter)))

      val fileReadActor = system.actorOf(Props[FileReadActor])

      fileReadActor.tell("/Othello.txt", controller)

      Thread.sleep(5000)

      controller ! "DISPLAY_LIST"
      expectMsg("DISPLAYED")
    }
  }
}

