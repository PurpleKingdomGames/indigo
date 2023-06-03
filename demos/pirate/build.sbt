import indigoplugin.ElectronInstall

Global / onChangedBuildSource := ReloadOnSourceChanges

//-----------------------------------
// The essentials.
//-----------------------------------

val scala3Version = "3.3.0"

lazy val pirate =
  (project in file("."))
    .enablePlugins(
      ScalaJSPlugin, // Enable the Scala.js
      SbtIndigo      //  Enable Indigo plugin
    )
    .settings( // Standard SBT settings
      name         := "pirate",
      version      := "0.0.1",
      scalaVersion := scala3Version,
      organization := "pirate",
      libraryDependencies ++= Seq(
        "org.scalameta"  %%% "munit"      % "0.7.29" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
      ),
      Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings( // Indigo specific settings
      showCursor            := true,
      title                 := "The Cursed Pirate",
      gameAssetsDirectory   := "assets",
      windowStartWidth      := 1280,
      windowStartHeight     := 720,
      disableFrameRateLimit := false,
      electronInstall       := ElectronInstall.Latest,
      backgroundColor       := "black",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % IndigoVersion.getVersion, // Needed for Aseprite & Tiled support
        "io.indigoengine" %%% "indigo"            % IndigoVersion.getVersion, // Important! :-)
        "io.indigoengine" %%% "indigo-extras"     % IndigoVersion.getVersion  // Important! :-)
      )
    )

addCommandAlias("buildGame", ";compile;fastLinkJS;indigoBuild")
addCommandAlias("buildGameFull", ";compile;fullLinkJS;indigoBuildFull")
addCommandAlias("runGame", ";compile;fastLinkJS;indigoRun")
addCommandAlias("runGameFull", ";compile;fullLinkJS;indigoRunFull")
