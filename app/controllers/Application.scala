package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor._
import java.text.SimpleDateFormat
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


class TrackActor extends Actor {
  val logger = Logger("tracker")
  def now=System.currentTimeMillis()
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z")
  var ips=Set.empty[String]
  var last:Option[String]=None
  def receive = {
    case _:StatsRequest => sender ! Stats(ips.size, last)
    case t:TrackIt =>
      ips += t.remoteAddress
      val trackDate = sdf.format(now)
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
         .mkString("NEW TRACKING INFO\n", "\n", "\n----------------------------------")
      logger.info(msg)
  }
}

case class TrackIt(remoteAddress:String, entries:Map[String,Seq[String]])

case class StatsRequest()

case class Stats(ipCount:Int=0, lastTimestamp:Option[String]=None)


object Application extends Controller {

  val tracker = Akka.system.actorOf(Props[TrackActor], name = "trackActor")
  
  def index = Action.async {
    implicit val timeout = Timeout(5.seconds)
    val futStats = tracker ? StatsRequest()
    futStats.map {
      case s:Stats => Ok(views.html.index(s))
      case _ => Ok(views.html.index(Stats()))
    }
    
  }

  def track = Action { request =>
    val content = request.body.asFormUrlEncoded
    content.map {entries =>
      tracker ! TrackIt(request.remoteAddress, entries)
      Ok(s"Success - ${entries.size} entries received.\n")
    }.getOrElse {
      BadRequest("Expecting form url encoded body")
    }
  }
}
