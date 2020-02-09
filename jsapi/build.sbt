import scala.sys.process._
import scala.language.postfixOps

val indigoVersion = "0.0.12-SNAPSHOT"

val silencerVersion = "1.4.4"

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.10",
  organization := "indigo-js",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.6.9" % "test"
  ),
  libraryDependencies ++= Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
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

lazy val indigojs =
  project
    .settings(commonSettings: _*)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      libraryDependencies += "indigo" %%% "indigo-exts" % "0.0.12-SNAPSHOT"
    )

lazy val apigen =
  project
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        // "com.lihaoyi" %% "fastparse" % "2.1.3",
        "org.tpolecat" %% "atto-core" % "0.7.0",
        "com.lihaoyi"  %% "os-lib"    % "0.6.2"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % "0.12.3")
    )

lazy val jsapi =
  (project in file("."))
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! }
    )
    .aggregate(
      apigen,
      indigojs
    )

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
