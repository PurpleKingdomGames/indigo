// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.sys.process._
import scala.language.postfixOps

val indigoVersion = "0.0.11-SNAPSHOT"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := "2.12.8",
  organization := "indigo",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.6.9" % "test"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
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
  scalacOptions in (Test, test) ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8",                         // Specify character encoding used by source files.
    "-explaintypes",                 // Explain type errors in more detail.
    "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
    // "-Xcheckinit",                      // Wrap field accessors to throw an exception on uninitialized access.
    // "-Xfatal-warnings",                 // Fail the compilation if there are any warnings.
    "-Xfuture", // Turn on future language features.
    // "-Xlint:adapted-args",              // Warn if an argument list is modified to match the receiver.
    // "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    // "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
    // "-Xlint:delayedinit-select",        // Selecting member of DelayedInit.
    // "-Xlint:doc-detached",              // A Scaladoc comment appears to be detached from its element.
    // "-Xlint:inaccessible",              // Warn about inaccessible types in method signatures.
    // "-Xlint:infer-any",                 // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",     // Warn when non-nullary `def f()' overrides nullary `def f'.
    // "-Xlint:nullary-unit",              // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",        // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    // "-Xlint:private-shadow",            // A private field (or class parameter) shadows a superclass field.
    // "-Xlint:stars-align",               // Pattern sequence wildcard must align with sequence component.
    // "-Xlint:type-parameter-shadow",     // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match", // Pattern match may not be typesafe.
    // "-Yno-adapted-args",                // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification" // Enable partial unification in type constructor inference
    // "-Ywarn-dead-code",                 // Warn when dead code is identified.
    // "-Ywarn-extra-implicit",            // Warn when more than one implicit parameter section is defined.
    // "-Ywarn-inaccessible",              // Warn about inaccessible types in method signatures.
    // "-Ywarn-infer-any",                 // Warn when a type argument is inferred to be `Any`.
    // "-Ywarn-nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
    // "-Ywarn-nullary-unit",              // Warn when nullary methods return Unit.
    // "-Ywarn-numeric-widen",             // Warn when numerics are widened.
    // "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
    // "-Ywarn-unused:imports",            // Warn if an import selector is not referenced.
    // "-Ywarn-unused:locals",             // Warn if a local definition is unused.
    // "-Ywarn-unused:params",             // Warn if a value parameter is unused.
    // "-Ywarn-unused:patvars",            // Warn if a variable bound in a pattern is unused.
    // "-Ywarn-unused:privates",           // Warn if a private member is unused.
    // "-Ywarn-value-discard"              // Warn when non-Unit expression results are unused.
  ),
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Overloading,
    Wart.ImplicitParameter
  ),
  scalacOptions += "-Yrangepos"
)

// Examples
lazy val basicSetup =
  crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/basic-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "basic-setup",
      showCursor := true,
      title := "Basic Setup",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )
    .jvmSettings(
      run / fork := true,
      javaOptions ++= Seq(
        "-XstartOnFirstThread",
        "-Dorg.lwjgl.util.Debug=true",
        "-Dorg.lwjgl.util.DebugLoader=true"
      )
    )

lazy val subSystems =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/subsystems"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "subsystems",
      showCursor := true,
      title := "SubSystems Example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val scenesSetup =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/scenes-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "scenes-setup",
      showCursor := true,
      title := "Scene Manager Setup",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val text =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/text"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "text-example",
      showCursor := true,
      title := "Text example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val inputfield =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/inputfield"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "input-field-example",
      showCursor := true,
      title := "Input field example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val fullSetup =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/full-setup"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "full-setup",
      showCursor := true,
      title := "Full Setup",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val button =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/button"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "button-example",
      showCursor := true,
      title := "Button example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val graphic =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/graphic"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "graphic-example",
      showCursor := true,
      title := "Graphic example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val group =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/group"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "group-example",
      showCursor := true,
      title := "Group example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val sprite =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/sprite"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "sprite-example",
      showCursor := true,
      title := "Sprite example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val http =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/http"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "http-example",
      showCursor := true,
      title := "Http example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val websocket =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/websocket"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "websocket-example",
      showCursor := true,
      title := "WebSocket example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val automata =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/automata"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "automata-example",
      showCursor := true,
      title := "Automata example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val fireworks =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/fireworks"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "fireworks-example",
      showCursor := true,
      title := "Fireworks!",
      gameAssetsDirectory := "assets",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test"
      )
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

lazy val audio =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("examples/audio"))
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "audio-example",
      showCursor := true,
      title := "Audio example",
      gameAssetsDirectory := "assets"
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true
    )

// Indigo
lazy val indigo =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test"
      )
    )
    .dependsOn(shared)
    .dependsOn(indigoPlatforms)
lazy val indigoJS  = indigo.js
lazy val indigoJVM = indigo.jvm

// Indigo Extensions
lazy val indigoExts =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo-exts"))
    .settings(commonSettings: _*)
    .dependsOn(indigo)
    .dependsOn(circe9 % "provided")
    .settings(
      name := "indigo-exts",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test"
    )
lazy val indigoExtsJS  = indigoExts.js
lazy val indigoExtsJVM = indigoExts.jvm

// Indigo Extensions
lazy val indigoPlatforms =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("indigo-platforms"))
    .settings(commonSettings: _*)
    .settings(
      name := "indigo-platforms",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test"
      )
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.7"
      )
    )
    .jvmSettings(
      fork in run := true,
      javaOptions ++= Seq(
        "-XstartOnFirstThread",
        "-Dorg.lwjgl.util.Debug=true",
        "-Dorg.lwjgl.util.DebugLoader=true"
      ),
      libraryDependencies ++= Seq(
        "org.lwjgl"      % "lwjgl-opengl"     % "3.2.1",
        "org.lwjgl"      % "lwjgl-openal"     % "3.2.1",
        "org.lwjgl.osgi" % "org.lwjgl.stb"    % "3.2.1.1",
        "org.lwjgl.osgi" % "org.lwjgl.assimp" % "3.2.1.1",
        "org.lwjgl.osgi" % "org.lwjgl.glfw"   % "3.2.1.1",
        "org.lwjgl.osgi" % "org.lwjgl.opengl" % "3.2.1.1"
      )
    )
    .dependsOn(shared)
lazy val indigoPlatformsJS  = indigoPlatforms.js
lazy val indigoPlatformsJVM = indigoPlatforms.jvm

// Games
lazy val sandbox =
  crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .dependsOn(circe9)
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
    .dependsOn(circe9)
    .dependsOn(indigoExts)
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

lazy val framework =
  crossProject(JSPlatform)
    .withoutSuffixFor(JSPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .dependsOn(indigoExts)
    .dependsOn(circe9)
    .enablePlugins(SbtIndigo)
    .settings(
      name := "indigo-framework",
      showCursor := true,
      title := "Framework",
      gameAssetsDirectory := "assets"
    )
    .dependsOn(shared)

// Server
lazy val server =
  crossProject(JVMPlatform)
    .withoutSuffixFor(JVMPlatform)
    .crossType(CrossType.Pure)
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
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(
      name := "shared",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.0" % "test"
    )
lazy val sharedJS  = shared.js
lazy val sharedJVM = shared.jvm

// Circe 0.9.x
lazy val circe9 =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(
      name := "circe9",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.9.3")
    )
    .dependsOn(shared)
lazy val circe9JS  = circe9.js
lazy val circe9JVM = circe9.jvm

// Root
lazy val indigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! },
      openshareddocs := { "open -a Firefox shared/.jvm/target/scala-2.12/api/indigo/index.html" ! },
      openindigodocs := { "open -a Firefox indigo/.jvm/target/scala-2.12/api/indigo/index.html" ! },
      openindigoextsdocs := { "open -a Firefox indigo-exts/.jvm/target/scala-2.12/api/indigoexts/index.html" ! }
    )
    .aggregate(
      sharedJVM,
      indigoPlatformsJVM,
      circe9JVM,
      indigoJVM,
      indigoExtsJVM,
      sandboxJVM
    )

// Cross build version - better or worse?
// crossProject(JSPlatform, JVMPlatform)
//   .crossType(CrossType.Pure)
//   .in(file("."))
// .jsSettings(
//   concurrentRestrictions in Global += Tags.limit(ScalaJSTags.Link, 2)
// )
// .jvmSettings(...

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")

// Don't call this, call readdocs
lazy val openshareddocs =
  taskKey[Unit]("Open the Indigo Shared API docs in FireFox")

// Don't call this, call readdocs
lazy val openindigodocs =
  taskKey[Unit]("Open the Indigo API docs in FireFox")

// Don't call this, call readdocs
lazy val openindigoextsdocs =
  taskKey[Unit]("Open the Indigo Extensions API docs in FireFox")
