import scala.sys.process._
import scala.language.postfixOps

ThisBuild / versionScheme := Some("early-semver")

lazy val indigoVersion = IndigoVersion.getVersion

val scala3Version    = "3.0.0-RC2"
val scala213Version = "2.13.5"

lazy val scalaFixSettings: Seq[sbt.Def.Setting[_]] =
  Seq(
    scalacOptions ++= (
      if (scalaVersion.value.startsWith("3.")) Nil else Seq(s"-P:semanticdb:targetroot:${baseDirectory.value}/target/.semanticdb", "-Yrangepos")
    ),
    scalafixOnCompile := (if (scalaVersion.value.startsWith("3.")) false else true)
  )

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version := indigoVersion,
  scalaVersion := scala3Version,
  semanticdbEnabled := !scalaVersion.value.startsWith("3."),
  semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
  crossScalaVersions := Seq(scala3Version, scala213Version),
  organization := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.23" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  crossScalaVersions := Seq(scala3Version, scala213Version)
) ++ scalaFixSettings

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
      gameAssetsDirectory := "assets",
      windowStartWidth := 800,
      windowStartHeight := 600
    )
    .settings(
      publish := {},
      publishLocal := {}
    )
    .dependsOn(indigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)

// Indigo Extensions
lazy val indigoExtras =
  project
    .in(file("indigo-extras"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(indigoShared)
    .settings(
      name := "indigo-extras",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shader-library"
        ) { (files: Set[File]) =>
          ShaderLibraryGen.makeShaderLibrary("ExtrasShaderLibrary", "indigoextras.shaders", files, (Compile / sourceManaged).value).toSet
        }

        cachedFun(IO.listFiles((baseDirectory.value / "shader-library")).toSet).toSeq
      }.taskValue
    )

// Indigo
lazy val indigo =
  project
    .in(file("indigo"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .dependsOn(indigoPlatforms)
    .settings(
      name := "indigo",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
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
        "org.scalacheck" %%% "scalacheck"  % "1.15.3" % "test",
        ("org.scala-js"  %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13)
      )
    )
    .dependsOn(indigoShared)

// Shared
lazy val indigoShared =
  project
    .in(file("indigo-shared"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(publishSettings: _*)
    .settings(
      name := "indigo-shared",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shaders"
        ) { (files: Set[File]) =>
          ShaderGen.makeShader(files, (Compile / sourceManaged).value).toSet
        }

        cachedFun(IO.listFiles((baseDirectory.value / "shaders")).toSet).toSeq
      }.taskValue
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shader-library"
        ) { (files: Set[File]) =>
          ShaderLibraryGen.makeShaderLibrary("ShaderLibrary", "indigo.shaders", files, (Compile / sourceManaged).value).toSet
        }

        cachedFun(IO.listFiles((baseDirectory.value / "shader-library")).toSet).toSeq
      }.taskValue
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
        "io.circe" %%% "circe-core"   % "0.14.0-M5",
        "io.circe" %%% "circe-parser" % "0.14.0-M5"
      )
    )
    .dependsOn(indigoExtras)

lazy val indigoShaders =
  project
    .in(file("indigo-shaders"))
    .settings(
      name := "indigo-shaders",
      version := indigoVersion,
      scalaVersion := scala3Version,
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.23" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings(
      publish := {},
      publishLocal := {}
    )

// Root
lazy val indigoProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      code := { "code ." ! },
      openshareddocs := { "open -a Firefox indigo-shared/.jvm/target/scala-3.0.0-RC2/api/indigo/index.html" ! },
      openindigodocs := { "open -a Firefox indigo/.jvm/target/scala-3.0.0-RC2/api/indigo/index.html" ! },
      openindigoextsdocs := { "open -a Firefox indigo-exts/.jvm/target/scala-3.0.0-RC2/api/indigoexts/index.html" ! }
    )
    .aggregate(
      indigoShared,
      indigoPlatforms,
      indigo,
      indigoExtras,
      indigoJsonCirce,
      indigoShaders,
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
