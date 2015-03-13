
name := "track"

scalaVersion := "2.11.6"

version := "1.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.websudos"  %% "phantom-dsl"       % "1.5.0",
  "com.websudos"  %% "phantom-zookeeper" % "1.5.0"
)

resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "websudos-repo" at "http://maven.websudos.co.uk/ext-release-local"
