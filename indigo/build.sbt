// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.sys.process._
import scala.language.postfixOps

lazy val indigoVersion = IndigoVersion.getVersion

val scala2 = "2.13.2"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := scala2,
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.7.4" % "test"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
  scalacOptions in (Compile, compile) ++= Scalac213Options.scala213Compile,
  scalacOptions in (Test, test) ++= Scalac213Options.scala213Test,
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Overloading,
    Wart.ImplicitParameter
  ),
  scalacOptions += "-Yrangepos"
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo := sonatypePublishToBundle.value,
    publishMavenStyle := true,
    sonatypeProfileName := "io.indigoengine",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "indigo", "indigo@purplekingdomgames.com")),
    developers := List(
      Developer(id = "davesmith00000", name = "David Smith", email = "indigo@purplekingdomgames.com", url = url("https://github.com/davesmith00000"))
    )
  )
}

// Testing

lazy val sandbox =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name := "sandbox",
      showCursor := true,
      title := "Sandbox",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )
    .dependsOn(indigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonUPickle)
lazy val sandboxJS  = sandbox.js
lazy val sandboxJVM = sandbox.jvm

lazy val perf =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtIndigo)
    .settings(commonSettings: _*)
    .settings(
      name := "indigo-perf",
      showCursor := true,
      title := "Perf",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish := {},
      publishLocal := {}
    )
    .dependsOn(indigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)
lazy val perfJS  = perf.js
lazy val perfJVM = perf.jvm

// Indigo
lazy val indigoCore =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo-core"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-core",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      )
    )
    .dependsOn(shared)
    .dependsOn(indigoPlatforms)
lazy val indigoCoreJS  = indigoCore.js
lazy val indigoCoreJVM = indigoCore.jvm

// Indigo Extensions
lazy val indigoExtras =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo-extras"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(shared)
    .settings(
      name := "indigo-extras",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
    )
lazy val indigoExtrasJS  = indigoExtras.js
lazy val indigoExtrasJVM = indigoExtras.jvm

// Indigo Game
lazy val indigo =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(indigoCore)
    .settings(
      name := "indigo",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
    )
    .jvmSettings(
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
    )
lazy val indigoJS  = indigo.js
lazy val indigoJVM = indigo.jvm

// Indigo Facades
lazy val facades =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("facades"))
    .settings(publishSettings: _*)
    .settings(
      name := "facades",
      version := indigoVersion,
      scalaVersion := scala2,
      organization := "io.indigoengine",
      scalacOptions += "-Yrangepos",
      scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits")
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "1.0.0"
      )
    )
lazy val facadesJS  = facades.js
lazy val facadesJVM = facades.jvm

// Indigo Platforms
lazy val indigoPlatforms =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .in(file("indigo-platforms"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-platforms",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
      )
    )
    .settings(
      sourceGenerators in Compile += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shaders"
        ) { (files: Set[File]) =>
          ShaderGen.makeShader(files, (sourceManaged in Compile).value).toSet
        }

        cachedFun(IO.listFiles((baseDirectory.value / "shaders")).toSet).toSeq
      }.taskValue
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "1.0.0"
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
    .dependsOn(facades)
lazy val indigoPlatformsJS  = indigoPlatforms.js
lazy val indigoPlatformsJVM = indigoPlatforms.jvm

// Shared
lazy val shared =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "shared",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
    )
    .jvmSettings(
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
    )

lazy val sharedJS  = shared.js
lazy val sharedJVM = shared.jvm

// Circe
lazy val indigoJsonCirce =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo-json-circe"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-json-circe",
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.13.0")
    )
    .dependsOn(indigoExtras)
lazy val indigoJsonCirceJS  = indigoJsonCirce.js
lazy val indigoJsonCirceJVM = indigoJsonCirce.jvm

// uPickle
lazy val indigoJsonUPickle =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("indigo-json-upickle"))
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-json-upickle",
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "upickle" % "1.1.0"
      )
    )
    .dependsOn(indigoExtras)
lazy val indigoJsonUPickleJS  = indigoJsonUPickle.js
lazy val indigoJsonUPickleJVM = indigoJsonUPickle.jvm

// Root
lazy val indigoProject =
  (project in file("."))
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! },
      openshareddocs := { "open -a Firefox shared/.jvm/target/scala-2.13/api/indigo/index.html" ! },
      openindigodocs := { "open -a Firefox indigo/.jvm/target/scala-2.13/api/indigo/index.html" ! },
      openindigoextsdocs := { "open -a Firefox indigo-exts/.jvm/target/scala-2.13/api/indigoexts/index.html" ! }
    )
    .aggregate(
      sharedJS,
      indigoPlatformsJS,
      indigoJsonCirceJS,
      indigoCoreJS,
      indigoExtrasJS,
      indigoJS,
      facadesJS,
      sandboxJS,
      perfJS
    )

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
