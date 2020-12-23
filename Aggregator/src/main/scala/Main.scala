import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger

object Main extends App {

  val kafkaURL = "localhost:9092"

  val schema = StructType(
    StructField("state", StringType) ::
      StructField("country", StringType) :: Nil
  )

  val spark = SparkSession
    .builder()
    .appName("Aggregator")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("subscribe", "states")
    .option("startingOffsets", "earliest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", value = false)
    .load()
    .selectExpr("CAST(value AS STRING)")
    .select(from_json($"value", schema).alias("value"))
    .select($"value.*")
    .groupBy($"state")
    .count()
    .select($"state".as("key"), $"count".as("value"))
    .select($"key", $"value".cast("string"))
    .writeStream
    .outputMode("update")
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("topic", "counts")
    .option("checkpointLocation", "hdfs:///kafka-checkpoint-aggregator")
    .trigger(Trigger.ProcessingTime("1 minute"))
    .start()
    .awaitTermination()
    
}
