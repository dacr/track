package controllers

import akka.actor._
import java.text.SimpleDateFormat
import play.api._

import java.util.{ Date, UUID }
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._

import java.net.InetAddress
import scala.concurrent._
import scala.concurrent.duration._

import com.websudos.phantom.zookeeper.SimpleCassandraConnector

trait Connector extends SimpleCassandraConnector {
    val keySpace = "tracking"
}

object Connector extends Connector


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
  
  implicit val session = Connector.session
  
  def createTableFuture() = {
    val f = create.future()
    f.onFailure{case x => x.printStackTrace}
    f
  }
  Await.result(createTableFuture(), 5000.millis)
  
  def add(t:TrackThat) = {
     val fentries = for {
       (k,values) <- t.entries.toList
       } yield {
       values.toList match {
         case Nil => Nil
         case v::Nil => List(k->v)
         case all => all.zipWithIndex.map{case (v,i) => (k+"."+(i+1)) -> v}
       }
     }
     insert
        .value(_.id, UUID.randomUUID())
        .value(_.timestamp, new DateTime(t.when))
        .value(_.category, t.category)
        .value(_.inet, InetAddress.getByName(t.remoteAddress))
        .value(_.entries, fentries.flatten.toMap)
        .future()
  }
  
}



class TrackToCassandraActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  
  def receive = {
    case _:StatsRequest => sender ! Stats(0, None)
    case t:TrackThat =>
      TrackRecord.add(t).onFailure{case x => x.printStackTrace}
  }
}
