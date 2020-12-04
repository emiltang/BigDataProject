import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object GeoMapping extends App {

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

  val kafka = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "twitter")
    .option("startingOffsets", "earliest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", value = false)
    .option("includeHeaders", "true")
    .load()

  val df = kafka.select(from_json($"value".cast("string"), schema).alias("value"))

  df.writeStream
    .outputMode("append")
    .format("console")
    .start()
    .awaitTermination();

}
