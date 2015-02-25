
name := "track"

scalaVersion := "2.11.1"

version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.websudos"  %% "phantom-dsl"  % "1.5.0"
)
