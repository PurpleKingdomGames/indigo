import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

val scala3Version = "3.2.2"

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

lazy val commonGameSettings = Seq(
  disableFrameRateLimit := (sys.props("os.name").toLowerCase match {
    case x if x contains "windows" => false
    case _                         => false
  }),
  electronInstall := (sys.props("os.name").toLowerCase match {
    case x if x.contains("windows") || x.contains("linux") =>
      indigoplugin.ElectronInstall.Version("^23.0.0")

    case _ =>
      indigoplugin.ElectronInstall.Global
  }),
  showCursor          := true,
  gameAssetsDirectory := "assets"
)

// Examples
lazy val basicSetup =
  project
    .in(file("basic-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "basic-setup",
      title := "Basic Setup"
    )
    .settings(commonGameSettings: _*)

lazy val blending =
  project
    .in(file("blending"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "blending-example",
      title := "Blending example"
    )
    .settings(commonGameSettings: _*)

lazy val subSystems =
  project
    .in(file("subsystems"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "subsystems",
      title := "SubSystems Example"
    )
    .settings(commonGameSettings: _*)

lazy val scenesSetup =
  project
    .in(file("scenes-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "scenes-setup",
      title := "Scene Manager Setup"
    )
    .settings(commonGameSettings: _*)

lazy val text =
  project
    .in(file("text"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "text-example",
      title := "Text example"
    )
    .settings(commonGameSettings: _*)

lazy val inputfield =
  project
    .in(file("inputfield"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "input-field-example",
      title := "Input field example"
    )
    .settings(commonGameSettings: _*)

lazy val button =
  project
    .in(file("button"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "button-example",
      title := "Button example"
    )
    .settings(commonGameSettings: _*)

lazy val graphic =
  project
    .in(file("graphic"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "graphic-example",
      title := "Graphic example"
    )
    .settings(commonGameSettings: _*)

lazy val group =
  project
    .in(file("group"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "group-example",
      title := "Group example"
    )
    .settings(commonGameSettings: _*)

lazy val tiled =
  project
    .in(file("tiled"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "tiled-example",
      title             := "Tiled example",
      windowStartWidth  := 19 * 32,
      windowStartHeight := 11 * 32,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % IndigoVersion.getVersion
      )
    )
    .settings(commonGameSettings: _*)

lazy val sprite =
  project
    .in(file("sprite"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "sprite-example",
      title := "Sprite example"
    )
    .settings(commonGameSettings: _*)

lazy val http =
  project
    .in(file("http"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "http-example",
      title := "Http example"
    )
    .settings(commonGameSettings: _*)

lazy val websocket =
  project
    .in(file("websocket"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "websocket-example",
      title := "WebSocket example"
    )
    .settings(commonGameSettings: _*)

lazy val automata =
  project
    .in(file("automata"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "automata-example",
      title := "Automata example"
    )
    .settings(commonGameSettings: _*)

lazy val fireworks =
  project
    .in(file("fireworks"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "fireworks-example",
      title             := "Fireworks!",
      windowStartWidth  := 1280,
      windowStartHeight := 720,
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
      )
    )
    .settings(commonGameSettings: _*)

lazy val audio =
  project
    .in(file("audio"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "audio-example",
      title := "Audio example"
    )
    .settings(commonGameSettings: _*)

lazy val lighting =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name              := "lighting Example",
      title             := "Lighting",
      windowStartWidth  := 684,
      windowStartHeight := 384
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .settings(commonGameSettings: _*)

lazy val distortion =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name              := "distortion",
      title             := "Distortion Example",
      windowStartWidth  := 684,
      windowStartHeight := 384
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .settings(commonGameSettings: _*)

lazy val assetLoading =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name  := "assetLoading",
      title := "Asset Loading Example"
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .settings(commonGameSettings: _*)

lazy val effects =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name              := "effects",
      title             := "Effects Example",
      windowStartWidth  := 550,
      windowStartHeight := 400
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .settings(commonGameSettings: _*)

lazy val radio =
  project
    .in(file("radio"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name  := "radio-example",
      title := "Radio button example"
    )
    .settings(commonGameSettings: _*)

lazy val jobs =
  project
    .in(file("jobs"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "jobs-example",
      title             := "Job System Example",
      windowStartWidth  := 400,
      windowStartHeight := 400
    )
    .settings(commonGameSettings: _*)

lazy val confetti =
  project
    .in(file("confetti"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "confetti",
      title             := "Confetti",
      windowStartWidth  := 640,
      windowStartHeight := 480
    )
    .settings(commonGameSettings: _*)

lazy val inputmapper =
  project
    .in(file("inputmapper"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "inputmapper-example",
      title             := "Input Mapper Example",
      windowStartWidth  := 400,
      windowStartHeight := 400
    )
    .settings(commonGameSettings: _*)

lazy val mouseevents =
  project
    .in(file("mouseevents"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name              := "mouseevents-example",
      title             := "Mouse Events Example",
      windowStartWidth  := 550,
      windowStartHeight := 400
    )
    .settings(commonGameSettings: _*)

lazy val errors =
  project
    .in(file("errors"))
    .settings(commonSettings: _*)
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(
      name              := "errors",
      title             := "Error Handling",
      windowStartWidth  := 800,
      windowStartHeight := 800
    )
    .settings(commonGameSettings: _*)

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
