
from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming. import KafkaUtils

spark = SparkContext(appName="MapToLocation")
stream = StreamingContext(spark, batchDuration=10)

kafka = KafkaUtils.createDirectStream()