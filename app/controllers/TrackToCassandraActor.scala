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
  object category extends StringColumn(this) with PrimaryKey[String]
  object inet extends InetAddressColumn(this) with PrimaryKey[InetAddress]
  //object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Ascending
  object timestamp extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object entries extends MapColumn[TrackRecord, TrackModel, String, String](this)
  //object count extends CounterColumn(this)

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
  
  /**
   * flatten the map values, add an index as soon that an entry has multiple values
   */
  private def flattenMap(in:Map[String, Seq[String]]):List[(String, String)] = {
     val fentries = for {
       (k,values) <- in.toList
       } yield {
       values.toList match {
         case Nil => Nil
         case v::Nil => List(k->v)
         case all => all.zipWithIndex.map{case (v,i) => (k+"."+(i+1)) -> v}
       }
     }
     fentries.flatten    
  }
  
  def add(t:TrackThat) = {
    insert
        .value(_.id, UUID.randomUUID())
        .value(_.timestamp, new DateTime(t.when))
        .value(_.category, t.category)
        .value(_.inet, InetAddress.getByName(t.remoteAddress))
        .value(_.entries, flattenMap(t.entries).toMap)
        .future()
  }
  
  def howmany():Future[Option[Long]] = count.one()
  
  // TODO : poor implementation
  def distinctIpCount() = select(_.inet).fetch().map(_.distinct.size)
  
  // TODO : poor implementation + it fails if the table is empty...
  def latestEventTimestamp() = select(_.timestamp).fetch().map(_.maxBy (_.getMillis))
  
}



class TrackToCassandraActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  
  def receive = {
    case t:TrackThat =>
      TrackRecord.add(t).onFailure{case x => x.printStackTrace}
  }
}
