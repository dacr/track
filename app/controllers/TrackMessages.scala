package controllers

case class TrackThat(when:Long, category:String, remoteAddress:String, entries:Map[String,Seq[String]])

//case class StatsRequest()

case class Stats(eventCount:Long=0, ipCount:Long=0, lastTimestamp:Option[String]=None)
