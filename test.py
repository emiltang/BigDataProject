from pyspark.sql import SparkSession

spark = SparkSession.builder.appName("SimpleApp").getOrCreate()
textFile = spark.read.text("hdfs:///user/hadoop/books/alice.txt")

print(textFile.count())

