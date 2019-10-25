# Installation Guide

Fairly ordinary setup
Has some specific requirements
Only works with SBT!

## Scala.js Only.

### SBT Version

Indigo works best with the latest SBT. Add the following to `project/build.properties`:

```
sbt.version=1.2.8
```

### SBT Plugins

Add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27")

addSbtPlugin("indigo" % "sbt-indigo" % "0.0.10-SNAPSHOT")
```

### SBT Build

Example `build.sbt` file for the root of your project:

```scala
lazy val snake =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings( // Standard SBT settings
      name := "snake",
      version := "0.0.1",
      scalaVersion := "2.12.10",
      organization := "snake",
      libraryDependencies ++= Seq(
        "com.lihaoyi"    %%% "utest"      % "0.6.6"  % "test",
        "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test"
      ),
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "Snake",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++= Seq(
        "indigo" %%% "circe9"      % "0.0.10-SNAPSHOT", // Needed for Aseprite & Tiled support
        "indigo" %%% "indigo-exts" % "0.0.10-SNAPSHOT"  // Important! :-)
      )
    )
```
