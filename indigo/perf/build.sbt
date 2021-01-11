val dottyVersion    = "3.0.0-M3"
val scala213Version = "2.13.4"

lazy val scalaFixSettings: Seq[sbt.Def.Setting[_]] =
  Seq(
    scalacOptions ++= (
      if (isDotty.value) Nil else Seq(s"-P:semanticdb:targetroot:${baseDirectory.value}/target/.semanticdb", "-Yrangepos")
    ),
    scalafixOnCompile := (if (isDotty.value) false else true)
  )

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version := PerfIndigoVersion.getVersion,
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
    .in(file("."))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name := "perf",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets",
      windowStartWidth := 800,
      windowStartHeight := 600
    )
    .dependsOn(ProjectRef(file(".").getParentFile / "..", "indigo"))
    .dependsOn(ProjectRef(file(".").getParentFile / "..", "indigoExtras"))
    .dependsOn(ProjectRef(file(".").getParentFile / "..", "indigoJsonCirce"))

addCommandAlias(
  "buildGame",
  List(
    "clean",
    "fastOptJS",
    "indigoBuild"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "buildGameFull",
  List(
    "clean",
    "fullOptJS",
    "indigoBuildFull"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "runGame",
  List(
    "clean",
    "fastOptJS",
    "indigoRun"
  ).mkString(";", ";", "")
)

addCommandAlias(
  "runGameFull",
  List(
    "clean",
    "fullOptJS",
    "indigoRunFull"
  ).mkString(";", ";", "")
)
