

enablePlugins(ScalaJSPlugin)

name := "indigo"

organization := "com.purplekingdomgames"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  //  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test" withSources() withJavadoc()
)
