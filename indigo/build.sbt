import indigoplugin._
import scala.language.postfixOps
import Misc._

Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.3.1"

ThisBuild / versionScheme                                  := Some("early-semver")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / scalaVersion                                   := scala3Version

lazy val indigoVersion = IndigoVersion.getVersion
// For the docs site
lazy val indigoDocsVersion  = "0.15.1"
lazy val tyrianDocsVersion  = "0.8.0"
lazy val scalaJsDocsVersion = "1.13.2"
lazy val scalaDocsVersion   = "3.3.1"
lazy val sbtDocsVersion     = "1.9.6"
lazy val millDocsVersion    = "0.11.4"

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
    .enablePlugins(ScalaJSPlugin, ScalaUnidocPlugin)
    .settings(
      neverPublish,
      commonSettings,
      name        := "IndigoProject",
      code        := codeTaskDefinition,
      usefulTasks := customTasksAliases,
      presentationSettings(version),
      ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(sandbox, perf, shader, physics, docs)
    )
    .aggregate(indigo, indigoExtras, indigoJsonCirce, sandbox, perf, shader, physics, docs, benchmarks)

// Testing

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
          .withAssetDirectory("sandbox/assets/")
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
    .cursorHidden

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

lazy val jsdocs = project
  .settings(
    neverPublish,
    organization := "io.indigoengine",
    libraryDependencies ++= Dependencies.jsDocs.value,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "indigo-json-circe" % indigoDocsVersion,
      "io.indigoengine" %%% "indigo"            % indigoDocsVersion,
      "io.indigoengine" %%% "indigo-extras"     % indigoDocsVersion,
      "io.indigoengine" %%% "tyrian-io"         % tyrianDocsVersion // ,
      // "io.indigoengine" %%% "tyrian-indigo-bridge" % tyrianDocsVersion
    ),
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports,
      ScalacOptions.warnUnusedLocals
    )
  )
  .enablePlugins(ScalaJSPlugin)

lazy val docs = project
  .in(file("indigo-docs"))
  .enablePlugins(MdocPlugin)
  .settings(
    neverPublish,
    organization       := "io.indigoengine",
    mdocJS             := Some(jsdocs),
    mdocExtraArguments := List("--no-link-hygiene"),
    mdocVariables := Map(
      "VERSION"         -> indigoDocsVersion,
      "SCALAJS_VERSION" -> scalaJsDocsVersion,
      "SCALA_VERSION"   -> scalaDocsVersion,
      "SBT_VERSION"     -> sbtDocsVersion,
      "MILL_VERSION"    -> millDocsVersion,
      "js-opt"          -> "fast"
    ),
    Compile / tpolecatExcludeOptions ++= Set(
      ScalacOptions.warnValueDiscard,
      ScalacOptions.warnUnusedImports,
      ScalacOptions.warnUnusedLocals
    )
  )
  .settings(
    run / fork := true
  )

addCommandAlias(
  "gendocs",
  List(
    "cleanAll",
    "unidoc",   // Docs in ./target/scala-3.3.1/unidoc/
    "docs/mdoc" // Docs in ./indigo/indigo-docs/target/mdoc
  ).mkString(";", ";", "")
)
