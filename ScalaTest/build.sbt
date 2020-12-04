name := "Cleaner"

version := "0.1"

scalaVersion := "2.12.10"

val sparkVersion = "3.0.1"

libraryDependencies += "org.apache.spark" % "spark-core_2.12" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % sparkVersion % "provided"
