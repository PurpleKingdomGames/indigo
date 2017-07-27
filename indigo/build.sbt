

enablePlugins(ScalaJSPlugin)

name := "indigo"

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
)

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %%% "circe-core",
  "io.circe" %%% "circe-generic",
  "io.circe" %%% "circe-parser"
).map(_ % circeVersion)