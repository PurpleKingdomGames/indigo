val indigoVersion = "0.0.10-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.8",
  organization := "indigo",
  libraryDependencies ++= Seq(
    "org.scalatest"  %%% "scalatest"  % "3.0.5"  % "test",
    "com.lihaoyi" %%% "utest" % "0.6.6" % "test"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
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
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
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
  ),
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Overloading,
    Wart.ImplicitParameter
  ),
  scalacOptions += "-Yrangepos"
)

// Examples
lazy val basicSetup =
  (project in file("examples/basic-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "basic-setup",
      showCursor := true,
      title := "Basic Setup",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val subSystems =
  (project in file("examples/subsystems"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "subsystems",
      showCursor := true,
      title := "SubSystems Example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val scenesSetup =
  (project in file("examples/scenes-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "scenes-setup",
      showCursor := true,
      title := "Scene Manager Setup",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val text =
  (project in file("examples/text"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "text-example",
      showCursor := true,
      title := "Text example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val inputfield =
  (project in file("examples/inputfield"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "input-field-example",
      showCursor := true,
      title := "Input field example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val fullSetup =
  (project in file("examples/full-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "full-setup",
      showCursor := true,
      title := "Full Setup",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val button =
  (project in file("examples/button"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "button-example",
      showCursor := true,
      title := "Button example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val graphic =
  (project in file("examples/graphic"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "graphic-example",
      showCursor := true,
      title := "Graphic example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val group =
  (project in file("examples/group"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "group-example",
      showCursor := true,
      title := "Group example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val sprite =
  (project in file("examples/sprite"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "sprite-example",
      showCursor := true,
      title := "Sprite example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val http =
  (project in file("examples/http"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "http-example",
      showCursor := true,
      title := "Http example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val websocket =
  (project in file("examples/websocket"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "websocket-example",
      showCursor := true,
      title := "WebSocket example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val automata =
  (project in file("examples/automata"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "automata-example",
      showCursor := true,
      title := "Automata example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val audio =
  (project in file("examples/audio"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "audio-example",
      showCursor := true,
      title := "Audio example",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

// Indigo
lazy val indigo =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
        "org.scala-js"   %%% "scalajs-dom" % "0.9.2",
        "org.scalacheck" %%% "scalacheck"  % "1.13.4" % "test"
      )
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(shared)

// Shared
lazy val indigoExts =
  (project in file("indigo-exts"))
    .settings(commonSettings: _*)
    .dependsOn(indigo)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "indigo-exts",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test"
    )

// Games
lazy val sandbox =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "indigo-sandbox",
      showCursor := true,
      title := "Sandbox",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )

lazy val perf =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .settings(
      name := "indigo-perf",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets",
      scalaJSUseMainModuleInitializer := true
    )
    .enablePlugins(ScalaJSPlugin, SbtIndigo)

lazy val framework =
  project
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "indigo-framework",
      showCursor := true,
      title := "Framework",
      gameAssetsDirectory := "assets"
    )
    .dependsOn(shared)

// Server
lazy val server =
  project
    .settings(commonSettings: _*)
    .settings(
      name := "server",
      libraryDependencies ++= Seq(
        "org.http4s"       %% "http4s-blaze-server" % "0.18.12",
        "org.http4s"       %% "http4s-circe"        % "0.18.12",
        "org.http4s"       %% "http4s-dsl"          % "0.18.12",
        "ch.qos.logback"   % "logback-classic"      % "1.2.3",
        "com.github.cb372" %% "scalacache-core"     % "0.10.0",
        "com.github.cb372" %% "scalacache-redis"    % "0.10.0",
        "com.github.cb372" %% "scalacache-caffeine" % "0.10.0"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.9.3")
    )
    .dependsOn(shared)

// Shared
lazy val shared =
  project
    .settings(commonSettings: _*)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "shared",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.9.3")
    )

// Root
lazy val indigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .aggregate(shared, indigo, indigoExts) //core
    .aggregate(sandbox)
//    .aggregate(perf, button) //compile tests
//    .aggregate(sandbox, framework, server) //games
//    .aggregate(basicSetup, subSystems, fullSetup, scenesSetup, http, text, automata, graphic, sprite, websocket, inputfield, audio, group) //examples

addCommandAlias("buildIndigo", ";shared/compile;indigo/compile;indigoExts/compile")
addCommandAlias("buildDev", ";sandbox/compile;perf/compile;framework/compile;server/compile")
addCommandAlias(
  "buildExamples1",
  ";basicSetup/compile;subSystems/compile;scenesSetup/compile;fullSetup/compile;button/compile;http/compile;text/compile;automata/compile"
)
addCommandAlias("buildExamples2", ";graphic/compile;sprite/compile;websocket/compile;inputfield/compile;audio/compile;group/compile")
addCommandAlias("buildAllNoClean", ";buildIndigo;buildDev;buildExamples1;buildExamples2")
addCommandAlias("buildAll", ";clean;buildAllNoClean")

addCommandAlias("testIndigo", ";shared/test;indigo/test;indigoExts/test")
addCommandAlias("testDev", ";sandbox/test;perf/test;framework/test;server/test")
addCommandAlias("testExamples1", ";basicSetup/test;subSystems/test;scenesSetup/test;fullSetup/test;button/test;http/test;text/test;automata/test")
addCommandAlias("testExamples2", ";graphic/test;sprite/test;websocket/test;inputfield/test;audio/test;group/test")
addCommandAlias("testAllNoClean", ";testIndigo;testDev;testExamples1;testExamples2")
addCommandAlias("testAll", ";clean;testAllNoClean")

addCommandAlias("testCompileIndigo", ";shared/test:compile;indigo/test:compile;indigoExts/test:compile")
addCommandAlias("testCompileDev", ";sandbox/test:compile;perf/test:compile;framework/test:compile;server/test:compile")
addCommandAlias("testCompileExamples1", ";basicSetup/test:compile;subSystems/test:compile;scenesSetup/test:compile;fullSetup/test:compile;button/test:compile;http/test:compile;text/test:compile;automata/test:compile")
addCommandAlias("testCompileExamples2", ";graphic/test:compile;sprite/test:compile;websocket/test:compile;inputfield/test:compile;audio/test:compile;group/test:compile")
addCommandAlias("testCompileAllNoClean", ";testCompileIndigo;testCompileDev;testCompileExamples1;testCompileExamples2")
addCommandAlias("testCompileAll", ";clean;testCompileAllNoClean")

addCommandAlias("localPublish", ";clean;buildIndigo;shared/publishLocal;indigo/publishLocal;indigoExts/publishLocal")

addCommandAlias("sandboxBuild", ";buildIndigo;shared/compile;indigo/compile;indigoExts/compile;sandbox/test;sandbox/fastOptJS;sandbox/indigoBuild")
