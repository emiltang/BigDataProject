import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}


object GeoMapping {

  val topics = Array("twitter")

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream",
    "auto.offset.reset" -> "earliest"
    // "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  def main(args: Array[String]): Unit = {

    val schema = StructType(
      StructField(
        "data",
        StructType(
          StructField("id", StringType),
          StructField("text", StringType),
          StructField("author_id", StringType)
        )
      ),
      StructField(
        "includes",
        StructType(
          StructType(
            "users",
            List(
              StructType(
                StructField("id", StringType),
                StructField("name", StringType),
                StructField("location", StringType),
                StructField("usrname", StringType)
              )
            )
          )
        )
      ),
      StructField(
        "matching_rules",
        List(
          StructType(
            StructField("id", LongType),
            StructField("tag", StringType, true)
          )
        )
      )
    )

    val sparkConf = new SparkConf()
      .setAppName("GeoMapping")
      .setMaster("spark://node-master:7077")

    val streamingContext = new StreamingContext(sparkConf, Seconds(10))

    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    println("--- begin ---")

    stream.map(record => "key" + record.key() + " value" + record.value())

    //stream.foreachRDD(rdd => rdd.foreach(item => println(item)))

    stream.print()

    //stream.print()

    streamingContext.start()
    streamingContext.awaitTermination()

    println("hello world")
    println("---  end  ---")
  }

}
