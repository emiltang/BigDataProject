name := "Aggregator"

version := "0.1"

scalaVersion := "2.12.10"

val sparkVersion = "3.0.1"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.12" % sparkVersion % "provided",
  "org.apache.spark" % "spark-sql_2.12" % sparkVersion % "provided",
  "org.apache.spark" % "spark-sql-kafka-0-10_2.12" % sparkVersion % "provided"
)
