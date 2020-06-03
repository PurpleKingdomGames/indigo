// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.sys.process._
import scala.language.postfixOps

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.13.2",
  organization := "indigo-examples",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest"  % "0.7.4" % "test",
    "io.indigoengine" %%% "indigo" % "0.0.12-SNAPSHOT",
    "io.indigoengine" %%% "indigo-extras" % "0.0.12-SNAPSHOT"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  scalacOptions in (Compile, compile) ++= ScalacOptions.scala213Compile,
  scalacOptions in (Test, test) ++= ScalacOptions.scala213Test,
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Overloading,
    Wart.ImplicitParameter
  ),
  scalacOptions += "-Yrangepos"
)

// Examples
lazy val basicSetup =
  crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("basic-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "basic-setup",
      showCursor := true,
      title := "Basic Setup",
      gameAssetsDirectory := "assets"
    )
    .jvmSettings(
      run / fork := true,
      javaOptions ++= Seq(
        "-XstartOnFirstThread",
        "-Dorg.lwjgl.util.Debug=true",
        "-Dorg.lwjgl.util.DebugLoader=true"
      )
    )

lazy val subSystems =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("subsystems"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "subsystems",
      showCursor := true,
      title := "SubSystems Example",
      gameAssetsDirectory := "assets"
    )

lazy val scenesSetup =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("scenes-setup"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "scenes-setup",
      showCursor := true,
      title := "Scene Manager Setup",
      gameAssetsDirectory := "assets"
    )

lazy val text =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("text"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "text-example",
      showCursor := true,
      title := "Text example",
      gameAssetsDirectory := "assets"
    )

lazy val inputmapping =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("inputmapping"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "inputmapping-example",
      showCursor := true,
      title := "Input mapping example",
      gameAssetsDirectory := "assets"
    )

lazy val inputfield =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("inputfield"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "input-field-example",
      showCursor := true,
      title := "Input field example",
      gameAssetsDirectory := "assets"
    )

lazy val button =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("button"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "button-example",
      showCursor := true,
      title := "Button example",
      gameAssetsDirectory := "assets"
    )

lazy val graphic =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("graphic"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "graphic-example",
      showCursor := true,
      title := "Graphic example",
      gameAssetsDirectory := "assets"
    )

lazy val group =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("group"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "group-example",
      showCursor := true,
      title := "Group example",
      gameAssetsDirectory := "assets"
    )

lazy val sprite =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("sprite"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "sprite-example",
      showCursor := true,
      title := "Sprite example",
      gameAssetsDirectory := "assets"
    )

lazy val http =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("http"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "http-example",
      showCursor := true,
      title := "Http example",
      gameAssetsDirectory := "assets"
    )

lazy val websocket =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("websocket"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "websocket-example",
      showCursor := true,
      title := "WebSocket example",
      gameAssetsDirectory := "assets"
    )

lazy val automata =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("automata"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "automata-example",
      showCursor := true,
      title := "Automata example",
      gameAssetsDirectory := "assets"
    )

lazy val fireworks =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("fireworks"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "fireworks-example",
      showCursor := true,
      title := "Fireworks!",
      gameAssetsDirectory := "assets",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      )
    )

lazy val audio =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("audio"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "audio-example",
      showCursor := true,
      title := "Audio example",
      gameAssetsDirectory := "assets"
    )

lazy val lighting =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "lighting Example",
      showCursor := true,
      title := "Lighting",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )

lazy val distortion =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "distortion",
      showCursor := true,
      title := "Distortion Example",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )

lazy val assetLoading =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "assetLoading",
      showCursor := true,
      title := "Asset Loading Example",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )

lazy val effects =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "effects",
      showCursor := true,
      title := "Effects Example",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )
