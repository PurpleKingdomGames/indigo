//-----------------------------------
// The essentials.
//-----------------------------------
lazy val pirate =
  (project in file("."))
    .settings(additionalSettings: _*)        // EXCEPT THIS LINE, comment this out if you don't need the stuff below!
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings(                               // Standard SBT settings
      name := "pirate",
      version := "0.0.1",
      scalaVersion := "2.13.1",
      organization := "pirate",
      libraryDependencies ++= Seq(
        "com.lihaoyi"    %%% "utest"      % "0.7.4"  % "test",
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "Pirate",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "indigo" %%% "circe12"      % "0.0.12-SNAPSHOT", // Needed for Aseprite & Tiled support
        "indigo" %%% "indigo-exts" % "0.0.12-SNAPSHOT"  // Important! :-)
      )
    )

//-----------------------------------
// Everything below here is optional!
// Stricter compiler settings and
// helper commands.
//-----------------------------------
import scala.sys.process._

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

lazy val openCoverageReportFirefox =
  taskKey[Unit]("Opens the coverage report in Firefox (mac)")

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuildJS")
addCommandAlias("buildGameFull", ";clean;update;compile;test;fastOptJS;indigoBuildJS")
addCommandAlias("publishGame", ";compile;fullOptJS;indigoPublishJS")
addCommandAlias("publishGameFull", ";clean;update;compile;test;fullOptJS;indigoPublishJS")

addCommandAlias(
  "testCoverage",
  List(
    "clean",
    "set coverageEnabled := true",
    "coverage",
    "test",
    "coverageReport",
    "set coverageEnabled := false",
    "openCoverageReportFirefox"
  ).mkString(";", ";", "")
)

lazy val additionalSettings = Seq(
  code := { "code ." ! },
  openCoverageReportFirefox := { "open -a Firefox target/scala-2.13/scoverage-report/index.html" ! },
  scalacOptions ++= Seq("-Yrangepos"),
  scalacOptions in (Compile, compile) ++= Scalac213Options.scala213Compile,
  scalacOptions in (Test, test) ++= Scalac213Options.scala213Test
)
