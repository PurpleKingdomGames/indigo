lazy val indigoVersion = "0.0.11-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.10",
  organization := "indigo",
  scalacOptions ++= ScalacOptions.scala212Compile
)

// Plugin
lazy val sbtIndigo =
  (project in file("sbt-indigo"))
    .settings(commonSettings: _*)
    .settings(
      name := "sbt-indigo",
      sbtPlugin := true,
      libraryDependencies ++= Seq(
        "commons-io" % "commons-io" % "2.6"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.9.3"),
      unmanagedSourceDirectories in Compile :=
        mirrorScalaSource((baseDirectory in ThisBuild).value.getParentFile / "sbt-indigo")
    )
    .enablePlugins(ScalaJSPlugin)

// Root
lazy val metaIndigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .settings(
      publish := {},
      publishLocal := {}
    )
    .dependsOn(sbtIndigo)
    .aggregate(sbtIndigo)

def mirrorScalaSource(baseDirectory: File): Seq[File] = {
  val scalaSourceDir = baseDirectory / "src" / "main" / "scala"
  if (scalaSourceDir.exists) scalaSourceDir :: Nil
  else sys.error(s"Missing source directory: $scalaSourceDir")
}
