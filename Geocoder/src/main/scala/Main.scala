package dk.sdu

import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import org.apache.http.impl.client.HttpClients
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf


object Main extends App {

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
        case _ => None
      }
    }
  }

  val spark = SparkSession
    .builder()
    .appName("GeoMapper")
    .master("local[*]")
    .getOrCreate()

  val geocode = udf {
    Nominamitm(_)
  }

  import spark.implicits._

  val in = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "locations")
    .option("startingOffsets", "earliest")
    // .option("max.poll.records", 10)
    //.option("failOnDataLoss", value = false)
    .load()

  val addresses = in
    .select($"value".cast("string"))
    .withColumn("value", geocode($"value"))

  //val json = addresses.select($"value.*").select(to_json(struct("state", "country")) as "value")

  addresses.writeStream
    .outputMode("append")
    .format("console")
    .option("truncate", "false")
    .start()
    .awaitTermination()

  //  json.writeStream
  //    .format("kafka")
  //    .option("kafka.bootstrap.servers",)
  //    .option("topic", "locations")
  //    .option("checkpointLocation", "hdfs:///kafka-checkpoint")
  //    .start()
  //    .awaitTermination()

  //case class Address(state: String = "", country: String = "")

}
