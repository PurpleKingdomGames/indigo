
val indigoVersion = "0.0.6-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.3",
  organization := "com.purplekingdomgames"
)

// Plugin
lazy val sbtIndigo =
  (project in file("sbt-indigo"))
    .settings(commonSettings: _*)
    .settings(
      name := "sbt-indigo",
      sbtPlugin := true,
      libraryDependencies ++= Seq(
        "commons-io" % "commons-io" % "2.5",
        "org.scalatest" %% "scalatest" % "3.0.1" % "test",
        "org.scala-js" %%% "scalajs-dom" % "0.9.1"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.8.0"),
      unmanagedSourceDirectories in Compile :=
        mirrorScalaSource((baseDirectory in ThisBuild).value.getParentFile / "sbt-indigo")
    )
    .enablePlugins(ScalaJSPlugin)

// Root
lazy val metaIndigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .dependsOn(sbtIndigo)
    .aggregate(sbtIndigo)

def mirrorScalaSource(baseDirectory: File): Seq[File] = {
  val scalaSourceDir = baseDirectory / "src" / "main" / "scala"
  if (scalaSourceDir.exists) scalaSourceDir :: Nil
  else sys.error(s"Missing source directory: $scalaSourceDir")
}

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.20")

addSbtPlugin("com.purplekingdomgames" % "sbt-indigo" % indigoVersion)
