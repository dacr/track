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
      val msg = 
        (t.entries + ("track.date"-> Seq(sdf.format(now))))
         .toList
         .flatMap{case (k,s) => s.map(k-> _)}
         .map{case (param, value) => s"$param->$value"}
         .mkString("NEW TRACKING INFO\n", "\n", "")
      logger.info(msg)
  }
}

case class TrackIt(entries:Map[String,Seq[String]])

object Application extends Controller {

  val tracker = Akka.system.actorOf(Props[TrackActor], name = "trackActor")
  
  def index = Action {
    Ok(views.html.index("Track is ready."))
  }

  def track = Action { request =>
    val content = request.body.asFormUrlEncoded
    content.map {entries =>
      tracker ! TrackIt(entries)
      Ok(s"Success - ${entries.size} entries received.\n")
    }.getOrElse {
      BadRequest("Expecting form url encoded body")
    }
  }
}
