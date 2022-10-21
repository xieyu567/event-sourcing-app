name := """event-sourcing-app"""
organization := "effe"
version := "1.0-SNAPSHOT"
scalaVersion := "2.12.17"

lazy val root = (project in file(".")).
  enablePlugins(PlayScala)

pipelineStages := Seq(digest)

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided",
  "org.postgresql" % "postgresql" % "42.4.2",
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.5.0",
  "ch.qos.logback"  %  "logback-classic" % "1.2.3",
  "de.svenkubiak" % "jBCrypt" % "0.4.3"
)
