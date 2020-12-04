import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Cleaner extends App {

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
    .appName("GeoMapper")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "twitter")
    .option("startingOffsets", "earliest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", value = false)
    .load()
    .select($"value".cast("string"))
    // Serialize Json
    .select(from_json($"value", schema).alias("value"))
    .select($"value.includes.users.location")
    // Unwrap json array
    .withColumn("location", explode($"location"))
    .filter($"location" =!= "null")
    .select(to_json(struct($"location")) as "value")
    .writeStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
    .option("topic", "locations")
    .start()
    .awaitTermination()
}
