package com.gosmarter.client

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.SynchronizedMap
import scala.io.Source

import org.slf4j.LoggerFactory

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

trait Logging {
  lazy val logger = LoggerFactory.getLogger(getClass)

  def debug(message: => String) = if (logger.isDebugEnabled) logger.debug(message)
}
/**
 * MapActor generate list of Results
 */
case class Result(word: String, count: Int) {
  override def toString = s"($count of $word)"
}

// Messages
case class DisplayList()
case class Displayed()

case class Line(text: String)
case class EOF()

case class File(filename: String)
case class Lines(lines: List[String])

case class AggregatedResults(results: Map[String, Int])
case class Results(results: List[Result])

/**
 * ControllerActor gets messages and forwards them to Aggregate or Map actor
 */
class ControllerActor(aggregateActor: ActorRef, mapRouter: ActorRef) extends Actor with Logging {
  def receive = {
    case DisplayList() => {
      debug("Display list")
      aggregateActor.tell(DisplayList(), sender)
      sender ! Displayed()
    }
    case Line(text) => mapRouter ! Line(text)
  }
}

/**
 * Reads a file and sends each line to ControllerActor
 */
class FileReadActor extends Actor with Logging {
  def receive = {
    case File(filename) => {
      debug(s"Read $filename")
      val lines = Source.fromURL(getClass.getResource(filename)).getLines

      lines foreach (sender ! Line(_))
      sender ! EOF()
    }
  }
}

/**
 * Sends each line of a list to ControllerActor
 */
class LineReadActor extends Actor with Logging {
  def receive = {
    case Lines(lines) => {
      lines foreach (sender ! Line(_))
      sender ! EOF()
    }
  }
}

/**
 * Aggregates results received from ReduceActors
 */
class AggregateActor extends Actor with Logging {
  var finalReducedMap: Map[String, Int] = new HashMap[String, Int] with SynchronizedMap[String, Int]

  def receive = {
    case DisplayList() => {
      debug(finalReducedMap.mkString("|"))
      sender ! AggregatedResults(finalReducedMap)
    }
    case AggregatedResults(results) => {
      debug(results.mkString("|"))
      results.foreach { case (word, count) => finalReducedMap update (word, count + finalReducedMap.getOrElseUpdate(word, 0)) }
    }
  }
}

/**
 * Reduces (word -> 1) lists to (word -> count) map
 */
class ReduceActor(aggregateActor: ActorRef) extends Actor with Logging {
  def receive = {
    case Results(results) => {
      debug(results.mkString("|"))
      aggregateActor ! AggregatedResults((Map.empty[String, Int] /: results) { case (m, r) => m + (r.word -> (m.getOrElseUpdate(r.word, 0) + r.count)) })
    }
  }
}

/**
 * Maps strings to (word -> 1) lists
 */
class MapActor(reducerActor: ActorRef) extends Actor with Logging {

  val STOP_WORDS: List[String] = List("a", "about", "above", "above", "across", "after",
    "afterwards", "again", "against", "all", "almost", "alone",
    "along", "already", "also", "although", "always", "am", "among",
    "amongst", "amoungst", "amount", "an", "and", "another", "any",
    "anyhow", "anyone", "anything", "anyway", "anywhere", "are",
    "around", "as", "at", "back", "be", "became", "because", "become",
    "becomes", "becoming", "been", "before", "beforehand", "behind",
    "being", "below", "beside", "besides", "between", "beyond", "bill",
    "both", "bottom", "but", "by", "call", "can", "cannot", "cant",
    "co", "con", "could", "couldnt", "cry", "de", "describe", "detail",
    "do", "done", "down", "due", "during", "each", "eg", "eight",
    "either", "eleven", "else", "elsewhere", "empty", "enough", "etc",
    "even", "ever", "every", "everyone", "everything", "everywhere",
    "except", "few", "fifteen", "fify", "fill", "find", "fire",
    "first", "five", "for", "former", "formerly", "forty", "found",
    "four", "from", "front", "full", "further", "get", "give", "go",
    "had", "has", "hasnt", "have", "he", "hence", "her", "here",
    "hereafter", "hereby", "herein", "hereupon", "hers", "herself",
    "him", "himself", "his", "how", "however", "hundred", "ie", "if",
    "in", "inc", "indeed", "interest", "into", "is", "it", "its",
    "itself", "keep", "last", "latter", "latterly", "least", "less",
    "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill",
    "mine", "more", "moreover", "most", "mostly", "move", "much",
    "must", "my", "myself", "name", "namely", "neither", "never",
    "nevertheless", "next", "nine", "no", "nobody", "none", "noone",
    "nor", "not", "nothing", "now", "nowhere", "of", "off", "often",
    "on", "once", "one", "only", "onto", "or", "other", "others",
    "otherwise", "our", "ours", "ourselves", "out", "over", "own",
    "part", "per", "perhaps", "please", "put", "rather", "re", "same",
    "see", "seem", "seemed", "seeming", "seems", "serious", "several",
    "she", "should", "show", "side", "since", "sincere", "six",
    "sixty", "so", "some", "somehow", "someone", "something",
    "sometime", "sometimes", "somewhere", "still", "such", "system",
    "take", "ten", "than", "that", "the", "their", "them",
    "themselves", "then", "thence", "there", "thereafter", "thereby",
    "therefore", "therein", "thereupon", "these", "they", "thickv",
    "thin", "third", "this", "those", "though", "three", "through",
    "throughout", "thru", "thus", "to", "together", "too", "top",
    "toward", "towards", "twelve", "twenty", "two", "un", "under",
    "until", "up", "upon", "us", "very", "via", "was", "we", "well",
    "were", "what", "whatever", "when", "whence", "whenever", "where",
    "whereafter", "whereas", "whereby", "wherein", "whereupon",
    "wherever", "whether", "which", "while", "whither", "who",
    "whoever", "whole", "whom", "whose", "why", "will", "with",
    "within", "without", "would", "yet", "you", "your", "yours",
    "yourself", "yourselves", "the")

  def isStopword(word: String) = STOP_WORDS.contains(word.toLowerCase)

  def receive = {
    case Line(text) => {
      debug(text)
      reducerActor ! Results(text.split(" ").toList filter (!isStopword(_)) map (new Result(_, 1)))
    }
  }
}