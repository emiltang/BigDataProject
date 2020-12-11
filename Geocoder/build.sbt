name := "Geocoder"

version := "0.1"

scalaVersion := "2.12.10"

idePackagePrefix := Some("dk.sdu")

val sparkVersion = "3.0.1"
val slf4j = "1.7.30"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided",
  "fr.dudie" % "nominatim-api" % "3.4",
  "org.apache.httpcomponents" % "httpclient" % "4.5.13",
  "org.slf4j" % "slf4j-api" % slf4j,
  "org.slf4j" % "slf4j-log4j12" % slf4j
)