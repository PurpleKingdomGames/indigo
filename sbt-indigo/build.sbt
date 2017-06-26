
name := "sbt-indigo"

scalaVersion := "2.10.6"

sbtPlugin := true

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.5",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
