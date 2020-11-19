from time import time

from pyspark.sql import SparkSession

DATA_FILE = 'hdfs:///user/hadoop/data/covid_twitter.json'
RESULT_LOCATION = 'hdfs:///user/hadoop/output/lang_results'

spark = SparkSession.builder.appName('SimpleApp').getOrCreate()
data = spark.read.json(DATA_FILE)
lang_count = data.groupBy('lang').count()

# lang_count.show()

lang_count.repartition(1).write.csv(path=RESULT_LOCATION, header="true")
