package controllers

import akka.actor._
import java.text.SimpleDateFormat
import play.api._

import java.util.{ Date, UUID }
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._

import java.net.InetAddress
import scala.concurrent.Await
import scala.concurrent.duration._

case class TrackModel (
  id: UUID,
  timestamp: DateTime,
  category: String,
  inet:InetAddress,
  entries: Map[String, String]
)


sealed class TrackRecord extends CassandraTable[TrackRecord, TrackModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending
  object category extends StringColumn(this)
  object inet extends InetAddressColumn(this)
  object entries extends MapColumn[TrackRecord, TrackModel, String, String](this)

  override def fromRow(row: Row): TrackModel = {
    TrackModel(id(row), timestamp(row), category(row), inet(row), entries(row));
  }
}

object TrackRecord extends TrackRecord {
  override val tableName="tracked"
  
  implicit val session = SomeCassandraClient.session
  
  Await.result(create.future(), 5000.millis)
}



class TrackToCassandraActor extends Actor {
  
  def receive = {
    case _:StatsRequest => sender ! Stats(0, None)
    case t:TrackThat =>
  }
}
