package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor._
import java.text.SimpleDateFormat

class TrackActor extends Actor {
  val logger = Logger("tracker")
  def now=System.currentTimeMillis()
  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z")
  def receive = {
    case t:TrackIt =>
      val entriesExts = List(
          "track.date"     -> Seq(sdf.format(now)),
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

object Application extends Controller {

  val tracker = Akka.system.actorOf(Props[TrackActor], name = "trackActor")
  
  def index = Action {
    Ok(views.html.index("Track is ready."))
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
