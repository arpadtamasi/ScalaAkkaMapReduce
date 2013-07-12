name := "ScalaAkkaMapReduce" 

version :="1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.0",  
  "com.typesafe.akka" %% "akka-agent" % "2.2.0",  
  "com.typesafe.akka" %% "akka-remote" % "2.2.0",
  "com.typesafe.akka" %% "akka-actor-tests" % "2.2.0",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
