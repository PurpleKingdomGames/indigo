
import IndigoSbtBuild._

// Indigo
lazy val indigo =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
        "org.scala-js" %%% "scalajs-dom" % "0.9.1"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.8.0")
    )
    .enablePlugins(ScalaJSPlugin)

// Games
lazy val sandbox =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "indigo-sandbox",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
      entryPoint := "com.example.sandbox.MyGame",
      showCursor := true,
      title := "Sandbox",
      gameAssetsDirectory := "assets"
    )

lazy val perf =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)
    .settings(
      name := "indigo-perf",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
      entryPoint := "com.example.perf.PerfGame",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets"
    )
    .enablePlugins(ScalaJSPlugin, SbtIndigo)

lazy val framework =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "indigo-framework",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
      entryPoint := "com.purplekingdomgames.indigoframework.Framework",
      showCursor := true,
      title := "Framework",
      gameAssetsDirectory := "assets"
    )

// Root
lazy val indigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(indigo, sandbox, perf, framework)
