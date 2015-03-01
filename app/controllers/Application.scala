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
  
  val logTracker = Akka.system.actorOf(Props[TrackToLogActor], name = "logTrackerActor")
  val dbTracker = Akka.system.actorOf(Props[TrackToCassandraActor], name = "databaseTrackerActor")
  
  def index = Action.async {
    implicit val timeout = Timeout(5.seconds)
    
    val eventCountFuture = TrackRecord.howmany()
    
    for {
      eventCount <- eventCountFuture
    } yield {
      val s = Stats(eventCount.getOrElse(0), 0,None)
      Ok(views.html.index(s))
    }
    
  }

  def track(category:String) = Action { request =>
    val content = request.body.asFormUrlEncoded
    content.map {entries =>
      val event = TrackThat(now(), category, request.remoteAddress, entries)
      logTracker ! event
      dbTracker ! event
      Ok(s"Success - ${entries.size} entries received.\n")
    }.getOrElse {
      BadRequest("Expecting form url encoded body")
    }
  }
}
