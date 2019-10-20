lazy val circeVersion = "0.11.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    inThisBuild(
      List(
        organization := "com.example",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.12.10"
      )
    ),
    name := "scala-elm",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.7",
      "org.scalatest" %%% "scalatest" % "3.0.5" % "test"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion)
  )
