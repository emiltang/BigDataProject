import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._



object Main extends App {

  val kafkaURL = "localhost:9092"

  val schema = StructType(
    StructField(
      "data",
      StructType(
        StructField("id", StringType) ::
          StructField("text", StringType) ::
          StructField("author_id", StringType) :: Nil
      )
    ) :: StructField(
      "includes",
      StructType(
        StructField(
          "users",
          ArrayType(
            StructType(
              StructField("id", StringType) ::
                StructField("name", StringType) ::
                StructField("location", StringType) ::
                StructField("username", StringType) :: Nil
            )
          )
        ) :: Nil
      )
    ) :: StructField(
      "matching_rules",
      ArrayType(
        StructType(
          StructField("id", LongType) ::
            StructField("tag", StringType, nullable = true) :: Nil
        )
      )
    ) :: Nil
  )

  val spark = SparkSession
    .builder()
    .appName("Cleaner")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("subscribe", "twitter")
    .option("startingOffsets", "earliest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", value = false)
    .load()
    .selectExpr("CAST(value AS STRING)")
    // Serialize Json
    .select(from_json($"value", schema).alias("value"))
    .select($"value.includes.users.location")
    // Unwrap json array
    .withColumn("location", explode($"location"))
    .filter($"location" =!= "null")
    .select($"location" as "value")
    .writeStream
    
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("topic", "locations")
    .option("checkpointLocation", "hdfs:///kafka-checkpoint")
    .start()
    .awaitTermination()
}
