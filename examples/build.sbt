import scala.sys.process._
import scala.language.postfixOps

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.13.3",
  organization := "indigo-examples",
  libraryDependencies ++= Seq(
    "com.lihaoyi"     %%% "utest"         % "0.7.4" % "test",
    "io.indigoengine" %%% "indigo"        % IndigoVersion.getVersion,
    "io.indigoengine" %%% "indigo-extras" % IndigoVersion.getVersion
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
  project
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

lazy val subSystems =
  project
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
  project
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
  project
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

lazy val inputfield =
  project
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
  project
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
  project
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
  project
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

lazy val tiled =
  project
    .in(file("tiled"))
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "tiled-example",
      showCursor := true,
      title := "Tiled example",
      gameAssetsDirectory := "assets",
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
      showCursor := true,
      title := "Sprite example",
      gameAssetsDirectory := "assets"
    )

lazy val http =
  project
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
  project
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
  project
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
  project
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
  project
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
  project
    .enablePlugins(SbtIndigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "lighting Example",
      showCursor := true,
      title := "Lighting",
      gameAssetsDirectory := "assets",
      windowStartWidth := 684,
      windowStartHeight := 384
    )
    .settings(
      publish := {},
      publishLocal := {}
    )

lazy val distortion =
  project
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
  project
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
  project
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
