name := "ScalaTest"

version := "0.1"

scalaVersion := "2.13.4"

val sparkVersion = "3.0.1"

libraryDependencies += "org.apache.spark" % "spark-core_2.12" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.12" % sparkVersion
