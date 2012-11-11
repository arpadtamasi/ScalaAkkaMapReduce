name := "ScalaAkkaMapReduce" 

version :="1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.2",  
  "com.typesafe.akka" % "akka-agent" % "2.0.2",  
  "com.typesafe.akka" % "akka-remote" % "2.0.2",
  "com.typesafe.akka" % "akka-actor-tests" % "2.0.2",
  "junit" % "junit" % "4.8.1" % "test",
  "org.scalatest" %% "scalatest" % "2.0.M4" % "test"
)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
