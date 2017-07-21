
enablePlugins(ScalaJSPlugin)

name := "indigo-framework"

libraryDependencies ++= Seq(
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
)

entryPoint := "com.purplekingdomgames.indigoframework.Framework"
showCursor := true
title := "Framework"
gameAssetsDirectory := "assets"
