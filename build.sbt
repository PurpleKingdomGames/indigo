
lazy val commonSettings = Seq(
  version := "0.0.5-SNAPSHOT",
  scalaVersion := "2.12.1",
  organization := "com.purplekingdomgames"
)

lazy val indigo =
  project
    .settings(commonSettings: _*)

lazy val sbtIndigo =
  (project in file("sbt-indigo"))
    .settings(commonSettings: _*)

lazy val sandbox =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)

lazy val perf =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)

lazy val framework =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigo)

lazy val indigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(indigo, sbtIndigo, sandbox, perf, framework)
