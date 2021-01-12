val dottyVersion    = "3.0.0-M3"
val scala213Version = "2.13.4"
val indigoVersion   = IndigoVersion.getVersion

lazy val scalaFixSettings: Seq[sbt.Def.Setting[_]] =
  Seq(
    scalacOptions ++= (
      if (isDotty.value) Nil else Seq(s"-P:semanticdb:targetroot:${baseDirectory.value}/target/.semanticdb", "-Yrangepos")
    ),
    scalafixOnCompile := (if (isDotty.value) false else true)
  )

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version := indigoVersion,
  scalaVersion := dottyVersion,
  crossScalaVersions := Seq(dottyVersion, scala213Version),
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.20" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  crossScalaVersions := Seq(dottyVersion, scala213Version),
  libraryDependencies ++= (if (isDotty.value) Nil else Seq(compilerPlugin(scalafixSemanticdb)))
) ++ scalaFixSettings

lazy val perf =
  project
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name := "perf",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets",
      windowStartWidth := 800,
      windowStartHeight := 600,
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % indigoVersion,
        "io.indigoengine" %%% "indigo"            % indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % indigoVersion
      )
    )

lazy val sandbox =
  project
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name := "sandbox",
      showCursor := true,
      title := "Sandbox",
      gameAssetsDirectory := "assets",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % indigoVersion,
        "io.indigoengine" %%% "indigo"            % indigoVersion,
        "io.indigoengine" %%% "indigo-extras"     % indigoVersion
      )
    )

lazy val testing =
  project
    .in(file("."))
    .aggregate(sandbox, perf)

addCommandAlias(
  "crossBuildAll",
  List(
    "+clean",
    "+test"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildGame",
  List(
    "sandbox/fastOptJS",
    "sandbox/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxBuildGameFull",
  List(
    "sandbox/fullOptJS",
    "sandbox/indigoBuildFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxRunGame",
  List(
    "sandbox/fastOptJS",
    "sandbox/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "sandboxRunGameFull",
  List(
    "sandbox/fullOptJS",
    "sandbox/indigoRunFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildGame",
  List(
    "perf/fastOptJS",
    "perf/indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfBuildGameFull",
  List(
    "perf/fullOptJS",
    "perf/indigoBuildFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfRunGame",
  List(
    "perf/fastOptJS",
    "perf/indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "perfRunGameFull",
  List(
    "perf/fullOptJS",
    "perf/indigoRunFull"
  ).mkString(";", ";", "")
)
