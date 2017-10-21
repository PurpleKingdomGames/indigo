
val indigoVersion = "0.0.6-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.3",
  organization := "com.purplekingdomgames",
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
  ),
  scalacOptions ++= Seq(
    //  "-Yno-imports", // Powerful but boring. There is another too called no-pref.
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-explaintypes",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    //  "-Xfatal-warnings", // Specifically NOT included.
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Overloading,
    Wart.FinalCaseClass,
    Wart.ImplicitConversion,
    Wart.Nothing,
    Wart.ImplicitParameter,
    Wart.NonUnitStatements,
    Wart.Equals,
    Wart.Recursion,
    Wart.LeakingSealed,
    Wart.Var
  )
)

// Indigo
lazy val indigo =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
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
