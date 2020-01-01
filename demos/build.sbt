// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.sys.process._
import scala.language.postfixOps

val silencerVersion = "1.4.4"

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.13.1",
  organization := "indigo-demos",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.6.9" % "test",
    "indigo"      %%% "circe12" % "0.0.12-SNAPSHOT",
    "indigo"      %%% "indigo-exts" % "0.0.12-SNAPSHOT"
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

// Games
lazy val sandbox =
  crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "sandbox",
      showCursor := true,
      title := "Sandbox",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )
lazy val sandboxJS  = sandbox.js
lazy val sandboxJVM = sandbox.jvm

lazy val perf =
  crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(
      name := "indigo-perf",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )
    .enablePlugins(SbtIndigo)


// Cross build version - better or worse?
// crossProject(JSPlatform, JVMPlatform)
//   .crossType(CrossType.Pure)
//   .in(file("."))
// .jsSettings(
//   concurrentRestrictions in Global += Tags.limit(ScalaJSTags.Link, 2)
// )
// .jvmSettings(...
