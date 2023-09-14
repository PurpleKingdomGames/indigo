import indigoplugin.IndigoGenerators
import indigoplugin.IndigoOptions

Global / onChangedBuildSource := ReloadOnSourceChanges

//-----------------------------------
// The essentials.
//-----------------------------------

val scala3Version = "3.3.0"

lazy val pirateOptions: IndigoOptions =
  IndigoOptions.defaults
    .withTitle("The Cursed Pirate")
    .withWindowWidth(1280)
    .withWindowHeight(720)
    .withBackgroundColor("black")
    .excludeAssetPaths {
      case p if p.contains("unused") => true
    }

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
      indigoOptions := pirateOptions,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % IndigoVersion.getVersion, // Needed for Aseprite & Tiled support
        "io.indigoengine" %%% "indigo"            % IndigoVersion.getVersion, // Important! :-)
        "io.indigoengine" %%% "indigo-extras"     % IndigoVersion.getVersion  // Important! :-)
      ),
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "pirate-gen"
        ) { _ =>
          IndigoGenerators
            .sbt((Compile / sourceManaged).value, "pirate.generated")
            .listAssets("GeneratedAssets", pirateOptions.assets)
            .toSourceFiles
            .toSet
        }
        cachedFun(IO.listFiles(baseDirectory.value / "assets").toSet).toSeq
      }
    )

addCommandAlias("buildGame", ";compile;fastLinkJS;indigoBuild")
addCommandAlias("buildGameFull", ";compile;fullLinkJS;indigoBuildFull")
addCommandAlias("runGame", ";compile;fastLinkJS;indigoRun")
addCommandAlias("runGameFull", ";compile;fullLinkJS;indigoRunFull")
