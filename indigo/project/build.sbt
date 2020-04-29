lazy val sbtIndigoVersion = "0.0.12-SNAPSHOT"

// Plugin
lazy val sbtIndigo =
  (project in file("sbt-indigo"))
    .settings(
      version := sbtIndigoVersion,
      scalaVersion := "2.12.10", // This is a plugin! Only 2.12 is supported!
      organization := "indigo",
      scalacOptions ++= Scalac212Options.scala212Compile
    )
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
      ).map(_ % "0.13.0"),
      unmanagedSourceDirectories in Compile :=
        mirrorScalaSource((baseDirectory in ThisBuild).value.getParentFile / "sbt-indigo")
    )
    .enablePlugins(ScalaJSPlugin)

def mirrorScalaSource(baseDirectory: File): Seq[File] = {
  val scalaSourceDir = baseDirectory / "src" / "main" / "scala"
  if (scalaSourceDir.exists) scalaSourceDir :: Nil
  else sys.error(s"Missing source directory: $scalaSourceDir")
}
