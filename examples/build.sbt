import indigoplugin.IndigoOptions
import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

val scala3Version = "3.3.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val commonSettings = Seq(
  version      := "0.0.1",
  scalaVersion := scala3Version,
  organization := "indigo-examples",
  libraryDependencies ++= Seq(
    "org.scalameta"   %%% "munit"         % "0.7.29" % Test,
    "io.indigoengine" %%% "indigo"        % IndigoVersion.getVersion,
    "io.indigoengine" %%% "indigo-extras" % IndigoVersion.getVersion
  ),
  scalacOptions ++= Seq("-language:strictEquality"),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

// Examples
lazy val basicSetup =
  project
    .in(file("basic-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "basic-setup",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Basic Setup")
          .withBackgroundColor("black")
          .withAssetDirectory("basic-setup/assets")
    )

lazy val blending =
  project
    .in(file("blending"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "blending-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Blending example")
          .withBackgroundColor("black")
          .withAssetDirectory("blending/assets")
    )

lazy val subSystems =
  project
    .in(file("subsystems"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "subsystems",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("SubSystems Example")
          .withBackgroundColor("black")
          .withAssetDirectory("subsystems/assets")
    )

lazy val scenesSetup =
  project
    .in(file("scenes-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "scenes-setup",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Scene Manager Setup")
          .withBackgroundColor("black")
          .withAssetDirectory("scenes-setup/assets")
    )

lazy val text =
  project
    .in(file("text"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "text-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Text example")
          .withBackgroundColor("black")
          .withAssetDirectory("text/assets")
    )

lazy val inputfield =
  project
    .in(file("inputfield"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "input-field-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Input field example")
          .withBackgroundColor("black")
          .withAssetDirectory("inputfield/assets")
    )

lazy val button =
  project
    .in(file("button"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "button-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Button example")
          .withBackgroundColor("black")
          .withAssetDirectory("button/assets")
    )

lazy val graphic =
  project
    .in(file("graphic"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "graphic-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Graphic example")
          .withBackgroundColor("black")
          .withAssetDirectory("graphic/assets")
    )

lazy val group =
  project
    .in(file("group"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "group-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Group example")
          .withBackgroundColor("black")
          .withAssetDirectory("group/assets")
    )

lazy val tiled =
  project
    .in(file("tiled"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "tiled-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Tiled example")
          .withBackgroundColor("black")
          .withAssetDirectory("tiled/assets")
          .withWindowStartWidth(19 * 32)
          .withWindowStartHeight(11 * 32),
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % IndigoVersion.getVersion
      )
    )

lazy val sprite =
  project
    .in(file("sprite"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "sprite-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Sprite example")
          .withBackgroundColor("black")
          .withAssetDirectory("sprite/assets")
    )

lazy val http =
  project
    .in(file("http"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "http-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Http example")
          .withBackgroundColor("black")
          .withAssetDirectory("http/assets")
    )

lazy val websocket =
  project
    .in(file("websocket"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "websocket-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("WebSocket example")
          .withBackgroundColor("black")
          .withAssetDirectory("websocket/assets")
    )

lazy val automata =
  project
    .in(file("automata"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "automata-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Automata example")
          .withBackgroundColor("black")
          .withAssetDirectory("automata/assets")
    )

lazy val fireworks =
  project
    .in(file("fireworks"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "fireworks-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Fireworks!")
          .withBackgroundColor("black")
          .withAssetDirectory("fireworks/assets")
          .withWindowStartWidth(1280)
          .withWindowStartHeight(720),
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
      )
    )

lazy val audio =
  project
    .in(file("audio"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "audio-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Audio example")
          .withBackgroundColor("black")
          .withAssetDirectory("audio/assets")
    )

lazy val lighting =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "lighting Example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Lighting")
          .withBackgroundColor("black")
          .withAssetDirectory("lighting/assets")
          .withWindowStartWidth(684)
          .withWindowStartHeight(384)
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val distortion =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "distortion",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Distortion Example")
          .withBackgroundColor("black")
          .withAssetDirectory("distortion/assets")
          .withWindowStartWidth(684)
          .withWindowStartHeight(384)
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val assetLoading =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "assetLoading",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Asset Loading Example")
          .withBackgroundColor("black")
          .withAssetDirectory("assetLoading/assets")
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val effects =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "effects",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Effects Example")
          .withBackgroundColor("black")
          .withAssetDirectory("effects/assets")
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val radio =
  project
    .in(file("radio"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "radio-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Radio button example")
          .withBackgroundColor("black")
          .withAssetDirectory("radio/assets")
    )

lazy val jobs =
  project
    .in(file("jobs"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "jobs-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Job System Example")
          .withBackgroundColor("black")
          .withAssetDirectory("jobs/assets")
          .withWindowStartWidth(400)
          .withWindowStartHeight(400)
    )

lazy val confetti =
  project
    .in(file("confetti"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "confetti",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Confetti")
          .withBackgroundColor("black")
          .withAssetDirectory("confetti/assets")
          .withWindowStartWidth(640)
          .withWindowStartHeight(480)
    )

lazy val inputmapper =
  project
    .in(file("inputmapper"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "inputmapper-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Input Mapper Example")
          .withBackgroundColor("black")
          .withAssetDirectory("inputmapper/assets")
          .withWindowStartWidth(400)
          .withWindowStartHeight(400)
    )

lazy val mouseevents =
  project
    .in(file("mouseevents"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "mouseevents-example",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Mouse Events Example")
          .withBackgroundColor("black")
          .withAssetDirectory("mouseevents/assets")
    )

lazy val errors =
  project
    .in(file("errors"))
    .settings(commonSettings: _*)
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "errors",
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Error Handling")
          .withBackgroundColor("black")
          .withAssetDirectory("errors/assets")
          .withWindowStartWidth(800)
          .withWindowStartHeight(800)
    )

// Root
lazy val examplesProject =
  (project in file("."))
    .settings(
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      }
    )
    .settings(
      logo := "Indigo Examples",
      usefulTasks := Seq(
        UsefulTask("", "cleanAll", "Clean all the projects"),
        UsefulTask("", "buildAllNoClean", "Rebuild without cleaning"),
        UsefulTask("", "testAllNoClean", "Test all without cleaning"),
        UsefulTask("", "crossLocalPublishNoClean", "Locally publish the core modules"),
        UsefulTask("", "code", "Launch VSCode")
      ) ++ makeCmds(ExampleProjects.exampleProjects),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.CYAN,
      commandColor     := scala.Console.BLUE,
      descriptionColor := scala.Console.WHITE
    )

def makeCmds(names: List[String]): Seq[UsefulTask] =
  names.zipWithIndex.map { case (n, i) =>
    val cmd = List(
      s"$n/fastOptJS",
      s"$n/indigoRun"
    ).mkString(";", ";", "")
    UsefulTask("run" + (i + 1), cmd, n)
  }.toSeq

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
