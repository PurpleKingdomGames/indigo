import indigoplugin._
import scala.language.postfixOps
import Misc._

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.6.4"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion  := scala3Version

inThisBuild(
  List(
    scalaVersion      := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixOnCompile := true
  )
)

lazy val indigoVersion = IndigoVersion.getVersion

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := indigoVersion,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Dependencies.commonSettings.value,
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  autoAPIMappings := true,
  logo            := name.value
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
    ),
    sonatypeCredentialHost := "oss.sonatype.org",
    sonatypeRepository     := "https://oss.sonatype.org/service/local"
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
          .withWindowSize(800, 600)
          .withTitle("Sandbox")
          .withBackgroundColor("black")
          .withAssetDirectory("sandbox/assets/"),
      Compile / sourceGenerators += Def.task {
        IndigoGenerators("example")
          .embedFont(
            "TestFont",
            os.pwd / "sandbox" / "assets" / "fonts" / "VCR_OSD_MONO_1.001.ttf",
            FontOptions(
              "test font",
              16,
              CharSet.ASCII
            )
              .withColor(RGB.White)
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
    .excludeAssets {
      case p if p.endsWith(os.RelPath.rel / ".DS_Store") => true
      case _                                             => false
    }

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
          .embedFont(
            moduleName = "PixelatedFont",
            font = os.pwd / "physics" / "generator-data" / "pixelated.ttf",
            fontOptions = FontOptions(
              fontKey = "Pixelated",
              fontSize = 32,
              charSet = CharSet.ASCII,
              color = RGB.White,
              antiAlias = false,
              layout = FontLayout.default
            ),
            imageOut = os.pwd / "physics" / "assets" / "generated"
          )
          .listAssets("Assets", physicsOptions.assets)
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
