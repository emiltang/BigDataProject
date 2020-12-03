import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.catalyst.dsl.expressions._
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.types._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

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

  val kafka = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("zookeeper.connect", "localhost:2181")
    .option("subscribe", "twitter")
    .option("startingOffsets", "earliest")
    .option("max.poll.records", 10)
    .option("failOnDataLoss", value = false)
    .load()

  import spark.implicits._

  val df = kafka.select(from_json($"value".cast(StringType), schema))
  
  display(df)

}
