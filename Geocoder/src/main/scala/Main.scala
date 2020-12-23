import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import org.apache.http.impl.client.HttpClients
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._



object Main extends App {

  val kafkaURL = "localhost:9092"

  val Nominamitm = new JsonNominatimClient(
    "http://node1:7070",
    HttpClients.createDefault,
    "emikr15@student.sdu.dk"
  )

  implicit class ExtendedNominatim(client: JsonNominatimClient) {
    def apply(query: String): Option[(String, String)] = {
      val request = new NominatimSearchRequest
      request.setAddress(true)
      request.setQuery(query)

      val result = client.search(request)

      if (result.size == 0) return None

      val address = result.get(0).getAddressElements

      val state = address.find(_.getKey == "state").map(_.getValue)
      val country = address.find(_.getKey == "country").map(_.getValue)

      (state, country) match {
        case (Some(state), Some(country)) => Some((state, country))
        case _                            => None
      }
    }
  }

  val geocode = udf { Nominamitm(_) }

  val spark = SparkSession
    .builder()
    .appName("Geocoder")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("subscribe", "locations")
    .option("startingOffsets", "latest")
    .load()
    .selectExpr("CAST(value AS STRING)")
    .withColumn("value", geocode($"value"))
    .filter($"value".isNotNull)
    .select($"value._1".as("state"), $"value._2".as("country"))
    .select(to_json(struct($"state", $"country")).as("value"))
    .writeStream
    .format("kafka")
    .option("kafka.bootstrap.servers", kafkaURL)
    .option("topic", "states")
    .option("checkpointLocation", "hdfs:///kafka-checkpoint-geocode")
    .start()
    .awaitTermination()

//     .writeStream
//     .outputMode("append")
//     .format("console")
//     .option("truncate", "false")
//     .start()
//     .awaitTermination()

}
