import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

val scala3Version = "3.1.2"

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
      name                := "basic-setup",
      showCursor          := true,
      title               := "Basic Setup",
      gameAssetsDirectory := "assets"
    )

lazy val blending =
  project
    .in(file("blending"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "blending-example",
      showCursor          := true,
      title               := "Blending example",
      gameAssetsDirectory := "assets"
    )

lazy val subSystems =
  project
    .in(file("subsystems"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "subsystems",
      showCursor          := true,
      title               := "SubSystems Example",
      gameAssetsDirectory := "assets"
    )

lazy val scenesSetup =
  project
    .in(file("scenes-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "scenes-setup",
      showCursor          := true,
      title               := "Scene Manager Setup",
      gameAssetsDirectory := "assets"
    )

lazy val text =
  project
    .in(file("text"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "text-example",
      showCursor          := true,
      title               := "Text example",
      gameAssetsDirectory := "assets"
    )

lazy val inputfield =
  project
    .in(file("inputfield"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "input-field-example",
      showCursor          := true,
      title               := "Input field example",
      gameAssetsDirectory := "assets"
    )

lazy val button =
  project
    .in(file("button"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "button-example",
      showCursor          := true,
      title               := "Button example",
      gameAssetsDirectory := "assets"
    )

lazy val graphic =
  project
    .in(file("graphic"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "graphic-example",
      showCursor          := true,
      title               := "Graphic example",
      gameAssetsDirectory := "assets"
    )

lazy val group =
  project
    .in(file("group"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "group-example",
      showCursor          := true,
      title               := "Group example",
      gameAssetsDirectory := "assets"
    )

lazy val tiled =
  project
    .in(file("tiled"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "tiled-example",
      showCursor          := true,
      title               := "Tiled example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 19 * 32,
      windowStartHeight   := 11 * 32,
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
      name                := "sprite-example",
      showCursor          := true,
      title               := "Sprite example",
      gameAssetsDirectory := "assets"
    )

lazy val http =
  project
    .in(file("http"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "http-example",
      showCursor          := true,
      title               := "Http example",
      gameAssetsDirectory := "assets"
    )

lazy val websocket =
  project
    .in(file("websocket"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "websocket-example",
      showCursor          := true,
      title               := "WebSocket example",
      gameAssetsDirectory := "assets"
    )

lazy val automata =
  project
    .in(file("automata"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "automata-example",
      showCursor          := true,
      title               := "Automata example",
      gameAssetsDirectory := "assets"
    )

lazy val fireworks =
  project
    .in(file("fireworks"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                  := "fireworks-example",
      showCursor            := true,
      title                 := "Fireworks!",
      gameAssetsDirectory   := "assets",
      windowStartWidth      := 1280,
      windowStartHeight     := 720,
      disableFrameRateLimit := true,
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
      name                := "audio-example",
      showCursor          := true,
      title               := "Audio example",
      gameAssetsDirectory := "assets"
    )

lazy val lighting =
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name                := "lighting Example",
      showCursor          := true,
      title               := "Lighting",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 684,
      windowStartHeight   := 384
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
      name                := "distortion",
      showCursor          := true,
      title               := "Distortion Example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 684,
      windowStartHeight   := 384
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
      name                := "assetLoading",
      showCursor          := true,
      title               := "Asset Loading Example",
      gameAssetsDirectory := "assets"
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
      name                := "effects",
      showCursor          := true,
      title               := "Effects Example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 550,
      windowStartHeight   := 400
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
      name                := "radio-example",
      showCursor          := true,
      title               := "Radio button example",
      gameAssetsDirectory := "assets"
    )

lazy val jobs =
  project
    .in(file("jobs"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "jobs-example",
      showCursor          := true,
      title               := "Job System Example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 400,
      windowStartHeight   := 400
    )

lazy val confetti =
  project
    .in(file("confetti"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "confetti",
      showCursor          := true,
      title               := "Confetti",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 640,
      windowStartHeight   := 480
    )

lazy val inputmapper =
  project
    .in(file("inputmapper"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "inputmapper-example",
      showCursor          := true,
      title               := "Input Mapper Example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 400,
      windowStartHeight   := 400
    )

lazy val mouseevents =
  project
    .in(file("mouseevents"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name                := "mouseevents-example",
      showCursor          := true,
      title               := "Mouse Events Example",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 550,
      windowStartHeight   := 400
    )

lazy val errors =
  project
    .in(file("errors"))
    .settings(commonSettings: _*)
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(
      name                := "errors",
      showCursor          := true,
      title               := "Error Handling",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 800,
      windowStartHeight   := 800
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
