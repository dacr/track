package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.actor.Props




object Application extends Controller {

  def now() = System.currentTimeMillis()
  
  val tracker = Akka.system.actorOf(Props[TrackToLogActor], name = "trackActor")
  
  def index = Action.async {
    implicit val timeout = Timeout(5.seconds)
    val futStats = tracker ? StatsRequest()
    futStats.map {
      case s:Stats => Ok(views.html.index(s))
      case _ => Ok(views.html.index(Stats()))
    }
    
  }

  def track(category:String) = Action { request =>
    val content = request.body.asFormUrlEncoded
    content.map {entries =>
      tracker ! TrackThat(now(), category, request.remoteAddress, entries)
      Ok(s"Success - ${entries.size} entries received.\n")
    }.getOrElse {
      BadRequest("Expecting form url encoded body")
    }
  }
}
