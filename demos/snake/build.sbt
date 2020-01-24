//-----------------------------------
// The essentials.
//-----------------------------------
lazy val snake =
  (project in file("."))
    .settings(additionalSettings: _*)        // EXCEPT THIS LINE, comment this out if you don't need the stuff below!
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings(                               // Standard SBT settings
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
        "indigo" %%% "circe12"      % "0.0.12-SNAPSHOT", // Needed for Aseprite & Tiled support
        "indigo" %%% "indigo-exts" % "0.0.12-SNAPSHOT"  // Important! :-)
      )
    )

//-----------------------------------
// Everything below here is optional!
// Stricter compiler settings and
// helper commands.
//-----------------------------------
import scala.sys.process._

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

lazy val openCoverageReportFirefox =
  taskKey[Unit]("Opens the coverage report in Firefox (mac)")

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuildJS")
addCommandAlias("buildGameFull", ";clean;update;compile;test;fastOptJS;indigoBuildJS")
addCommandAlias("publishGame", ";compile;fullOptJS;indigoPublishJS")
addCommandAlias("publishGameFull", ";clean;update;compile;test;fullOptJS;indigoPublishJS")

addCommandAlias(
  "testCoverage",
  List(
    "clean",
    "set coverageEnabled := true",
    "coverage",
    "test",
    "coverageReport",
    "set coverageEnabled := false",
    "openCoverageReportFirefox"
  ).mkString(";", ";", "")
)

lazy val additionalSettings = Seq(
  code := { "code ." ! },
  openCoverageReportFirefox := { "open -a Firefox target/scala-2.12/scoverage-report/index.html" ! },
  scalacOptions ++= Seq("-Yrangepos"),
  scalacOptions in (Compile, compile) ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8",                            // Specify character encoding used by source files.
    "-explaintypes",                    // Explain type errors in more detail.
    "-feature",                         // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",           // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros",    // Allow macro definition (besides implementation and application)
    "-language:higherKinds",            // Allow higher-kinded types
    "-language:implicitConversions",    // Allow definition of implicit functions called views
    "-unchecked",                       // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                      // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                 // Fail the compilation if there are any warnings.
    "-Xfuture",                         // Turn on future language features.
    "-Xlint:adapted-args",              // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associativeÌÌ operator.
    "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",        // Selecting member of DelayedInit.
    "-Xlint:doc-detached",              // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",              // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                 // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",      // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",              // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",           // Option.apply used implicit view.
    "-Xlint:package-object-classes",    // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",    // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",            // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",               // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",     // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",             // Pattern match may not be typesafe.
    "-Yno-adapted-args",                // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",            // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                 // Warn when dead code is identified.
    "-Ywarn-extra-implicit",            // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",              // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                 // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",              // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",             // Warn when numerics are widened.
    "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",            // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",             // Warn if a local definition is unused.
    "-Ywarn-unused:params",             // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",            // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",           // Warn if a private member is unused.
    "-Ywarn-value-discard"              // Warn when non-Unit expression results are unused.
  )
)
