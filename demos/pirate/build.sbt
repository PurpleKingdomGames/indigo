//-----------------------------------
// The essentials.
//-----------------------------------

val dottyVersion    = "3.0.0-RC2"

lazy val pirate =
  (project in file("."))
    .enablePlugins(
      ScalaJSPlugin, // Enable the Scala.js
      SbtIndigo      //  Enable Indigo plugin
    )
    .settings( // Standard SBT settings
      name := "pirate",
      version := "0.0.1",
      scalaVersion := dottyVersion,
      organization := "pirate",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.23" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      // wartremoverWarnings in (Compile, compile) ++= Warts.unsafe,
      Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "The Cursed Pirate",
      gameAssetsDirectory := "assets",
      windowStartWidth := 1280,
      windowStartHeight := 720,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % IndigoVersion.getVersion, // Needed for Aseprite & Tiled support
        "io.indigoengine" %%% "indigo"            % IndigoVersion.getVersion, // Important! :-)
        "io.indigoengine" %%% "indigo-extras"     % IndigoVersion.getVersion // Important! :-)
      )
    )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
