import scala.sys.process._
import scala.language.postfixOps

lazy val indigoVersion = IndigoVersion.getVersion

val dottyVersion    = "3.0.0-M2"
val scala213Version = "2.13.4"

lazy val commonSettings = Seq(
  version := indigoVersion,
  scalaVersion := dottyVersion,
  crossScalaVersions := Seq(dottyVersion, scala213Version),
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.19" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  crossScalaVersions := Seq(dottyVersion, scala213Version)
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
  project
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
    .dependsOn(indigoJsonCirce)

lazy val perf =
  project
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

// Indigo
lazy val indigoCore =
  project
    .in(file("indigo-core"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-core",
      libraryDependencies ++= Seq(
        ("org.scalacheck" %% "scalacheck" % "1.14.3" % "test").withDottyCompat(scalaVersion.value)
      )
    )
    .dependsOn(shared)
    .dependsOn(indigoPlatforms)

// Indigo Extensions
lazy val indigoExtras =
  project
    .in(file("indigo-extras"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(shared)
    .settings(
      name := "indigo-extras",
      libraryDependencies += ("org.scalacheck" %% "scalacheck" % "1.14.3" % "test").withDottyCompat(scalaVersion.value)
    )

// Indigo Game
lazy val indigo =
  project
    .in(file("indigo"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(indigoCore)
    .settings(
      name := "indigo",
      libraryDependencies += ("org.scalacheck" %% "scalacheck" % "1.14.3" % "test").withDottyCompat(scalaVersion.value)
    )

// Indigo Facades
lazy val facades =
  project
    .in(file("facades"))
    .enablePlugins(ScalaJSPlugin)
    .settings(publishSettings: _*)
    .settings(
      name := "facades",
      version := indigoVersion,
      scalaVersion := dottyVersion,
      crossScalaVersions := Seq(dottyVersion, scala213Version),
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0").withDottyCompat(scalaVersion.value)
      )
    )

// Indigo Platforms
lazy val indigoPlatforms =
  project
    .in(file("indigo-platforms"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-platforms",
      libraryDependencies ++= Seq(
        ("org.scalacheck" %% "scalacheck"  % "1.14.3" % "test").withDottyCompat(scalaVersion.value),
        ("org.scala-js"  %%% "scalajs-dom" % "1.1.0").withDottyCompat(scalaVersion.value)
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
    .dependsOn(shared)
    .dependsOn(facades)

// Shared
lazy val shared =
  project
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "shared",
      libraryDependencies += ("org.scalacheck" %%% "scalacheck" % "1.14.3" % "test").withDottyCompat(scalaVersion.value)
    )

// Circe
lazy val indigoJsonCirce =
  project
    .in(file("indigo-json-circe"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-json-circe",
      libraryDependencies ++= Seq(
        ("io.circe" %%% "circe-core"   % "0.13.0").withDottyCompat(scalaVersion.value),
        ("io.circe" %%% "circe-parser" % "0.13.0").withDottyCompat(scalaVersion.value)
      )
    )
    .dependsOn(indigoExtras)

// Root
lazy val indigoProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! },
      openshareddocs := { "open -a Firefox shared/.jvm/target/scala-3.0.0-M2/api/indigo/index.html" ! },
      openindigodocs := { "open -a Firefox indigo/.jvm/target/scala-3.0.0-M2/api/indigo/index.html" ! },
      openindigoextsdocs := { "open -a Firefox indigo-exts/.jvm/target/scala-3.0.0-M2/api/indigoexts/index.html" ! }
    )
    .aggregate(
      shared,
      indigoPlatforms,
      indigoJsonCirce,
      indigoCore,
      indigoExtras,
      indigo,
      facades,
      sandbox,
      perf
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
