package controllers

import akka.actor._
import java.text.SimpleDateFormat
import play.api._


class TrackToCassandraActor extends Actor {
  def receive = {
    case _:StatsRequest => sender ! Stats(0, None)
    case t:TrackThat =>
  }
}
