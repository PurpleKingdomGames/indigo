
val indigoVersion = "0.0.6-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.3",
  organization := "com.purplekingdomgames",
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
    "-language:higherKinds",             // Allow higher-kinded types
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
    "-Xfuture",                          // Turn on future language features.
    "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
    "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",            // Warn if a private member is unused.
    "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
  )
)

// Indigo
lazy val indigo =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
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
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
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
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
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
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
      ),
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
