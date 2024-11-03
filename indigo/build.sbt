import indigoplugin._
import scala.language.postfixOps
import Misc._

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.4.1"

ThisBuild / versionScheme                                  := Some("early-semver")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / scalaVersion                                   := scala3Version

lazy val indigoVersion = IndigoVersion.getVersion

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := indigoVersion,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Dependencies.commonSettings.value,
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  autoAPIMappings   := true,
  logo              := name.value
)

lazy val neverPublish = Seq(
  publish / skip      := true,
  publishLocal / skip := true
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo              := sonatypePublishToBundle.value,
    publishMavenStyle      := true,
    sonatypeProfileName    := "io.indigoengine",
    licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "indigo", "indigo@purplekingdomgames.com")),
    developers := List(
      Developer(
        id = "davesmith00000",
        name = "David Smith",
        email = "indigo@purplekingdomgames.com",
        url = url("https://github.com/davesmith00000")
      )
    )
  )
}

// Root
lazy val indigoProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      neverPublish,
      commonSettings,
      name        := "IndigoProject",
      code        := codeTaskDefinition,
      usefulTasks := customTasksAliases,
      presentationSettings(version)
    )
    .aggregate(
      indigo,
      indigoExtras,
      indigoJsonCirce,
      tyrianIndigoBridge,
      sandbox,
      perf,
      shader,
      physics,
      benchmarks,
      tyrianSandbox
    )

// Testing

lazy val tyrianSandbox =
  (project in file("tyrian-sandbox"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(indigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .dependsOn(tyrianIndigoBridge)
    .settings(
      neverPublish,
      commonSettings,
      name := "tyrian-sandbox",
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-io" % Dependencies.Versions.tyrianVersion
      ),
      scalacOptions -= "-language:strictEquality"
    )

lazy val sandbox =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .settings(
      neverPublish,
      commonSettings,
      name := "sandbox",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Sandbox")
          .withBackgroundColor("black")
          .withAssetDirectory("sandbox/assets/"),
      Compile / sourceGenerators += Def.task {
        IndigoGenerators("example")
          .embedFont(
            "TestFont",
            os.pwd / "sandbox" / "assets" / "fonts" / "pixelated.ttf",
            FontOptions(
              "test font",
              32,
              CharSet.fromUniqueString("The quick brown fox\njumps over the\nlazy dog.")
            )
              .withColor(RGB.White)
              .withMaxCharactersPerLine(16)
              .noAntiAliasing,
            os.pwd / "sandbox" / "assets" / "generated"
          )
          .toSourceFiles((Compile / sourceManaged).value)
      }
    )

lazy val perf =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .settings(
      neverPublish,
      commonSettings,
      name := "indigo-perf",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Perf")
          .withBackgroundColor("black")
          .withWindowWidth(800)
          .withWindowHeight(600)
          .withAssetDirectory("perf/assets/")
    )

lazy val shader =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .settings(
      neverPublish,
      commonSettings,
      name := "indigo-shader",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Shader")
          .withBackgroundColor("black")
          .withWindowWidth(450)
          .withWindowHeight(450)
          .withAssetDirectory("shader/assets/")
    )

lazy val physicsOptions =
  IndigoOptions.defaults
    .withTitle("Physics")
    .withBackgroundColor("black")
    .withAssetDirectory("physics/assets/")
    .withWindowSize(800, 600)

lazy val physics =
  project
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .settings(
      neverPublish,
      commonSettings,
      name          := "physics",
      indigoOptions := physicsOptions,
      Compile / sourceGenerators += Def.task {
        IndigoGenerators("example")
          .generateConfig("Config", physicsOptions)
          .toSourceFiles((Compile / sourceManaged).value)
      }
    )

// Indigo Extensions
lazy val indigoExtras =
  project
    .in(file("indigo-extras"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(indigo)
    .settings(
      name := "indigo-extras",
      libraryDependencies ++= Dependencies.indigoExtras.value,
      commonSettings ++ publishSettings
    )

lazy val tyrianIndigoBridge =
  project
    .in(file("tyrian-indigo-bridge"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(indigo)
    .settings(
      name := "tyrian-indigo-bridge",
      commonSettings ++ publishSettings,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-io" % Dependencies.Versions.tyrianVersion
      )
    )

// Indigo
lazy val indigo =
  project
    .in(file("indigo"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "indigo",
      commonSettings ++ publishSettings,
      libraryDependencies ++= Dependencies.indigo.value
    )

// Circe
lazy val indigoJsonCirce =
  project
    .in(file("indigo-json-circe"))
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(indigo)
    .settings(
      name := "indigo-json-circe",
      commonSettings ++ publishSettings,
      libraryDependencies ++= Dependencies.indigoJsonCirce.value
    )

lazy val benchmarks =
  project
    .in(file("benchmarks"))
    .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
    .settings(
      neverPublish,
      name         := "indigo-benchmarks",
      version      := indigoVersion,
      organization := "io.indigoengine",
      Test / test  := {},
      libraryDependencies ++= Dependencies.benchmark.value,
      jsDependencies ++= Dependencies.benchmarkJs.value
    )

addCommandAlias(
  "tyrianSandboxBuild",
  List(
    "tyrianSandbox/fastLinkJS"
  ).mkString("", ";", ";")
)
