
enablePlugins(ScalaJSPlugin)

name := "indigo-sandbox"

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
)

entryPoint := "com.example.sandbox.MyGame"
showCursor := true
title := "Sandbox"
gameAssetsDirectory := "assets"
