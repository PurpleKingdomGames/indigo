

enablePlugins(ScalaJSPlugin)

name := "indigo"

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "upickle" % "0.4.4"
)
