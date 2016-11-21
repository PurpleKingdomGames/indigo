

enablePlugins(ScalaJSPlugin)

name := "scalajs-game"

organization := "com.example"

version := "0.0.1"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scala-js" % "scalajs-dom_sjs0.6_2.12" % "0.9.1"
  //  "org.scalacheck" %% "scalacheck" % "1.12.1" % "test" withSources() withJavadoc()
)
