#!/usr/bin/env bash

CURRENT_DIR=`dirname "$0"` 

sbt package

spark-submit \
	--packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1,fr.dudie:nominatim-api:3.4 \
	--class Main \
	--executor-memory 1G \
	$CURRENT_DIR/target/scala-2.12/geocoder_2.12-0.1.jar
