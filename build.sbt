name := """event-sourcing-app"""

val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.10",
  organization := "effe"
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .aggregate(events)
  .dependsOn(events)

lazy val events = (project in file("events"))
  .settings(commonSettings)
  .settings(Seq(libraryDependencies := Seq("com.typesafe.play" %% "play-json" % "2.9.3")))

pipelineStages := Seq(digest)

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided",
  "org.postgresql" % "postgresql" % "42.5.0",
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.4.4",
  "de.svenkubiak" % "jBCrypt" % "0.4.3",
  "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.1"
)
