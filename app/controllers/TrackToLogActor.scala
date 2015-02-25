package controllers

import akka.actor._
import java.text.SimpleDateFormat
import play.api._


class TrackToLogActor extends Actor {
  val logger = Logger("tracker")
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z")
  var ips=Set.empty[String]
  var last:Option[String]=None
  def receive = {
    case _:StatsRequest => sender ! Stats(ips.size, last)
    case t:TrackThat =>
      ips += t.remoteAddress
      val trackDate = sdf.format(t.when)
      last = Some(trackDate)
      val entriesExts = List(
          "track.date"     -> Seq(trackDate),
          "remote.address" -> Seq(t.remoteAddress)
          )
      val msg = 
        (t.entries.toList ++ entriesExts)
         .flatMap{case (k,s) => s.map(k-> _)}
         .sortBy{case (k,v) => k}
         .map{case (param, value) => s"$param->$value"}
         .mkString(s"NEW '${t.category}' TRACKING INFO\n", "\n", "\n----------------------------------")
      logger.info(msg)
  }
}
