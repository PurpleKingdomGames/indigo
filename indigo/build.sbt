import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / versionScheme                                  := Some("early-semver")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val indigoVersion = IndigoVersion.getVersion
// For the docs site
lazy val indigoDocsVersion  = "0.10.0"
lazy val scalaJsDocsVersion = "1.8.0"
lazy val scalaDocsVersion   = "3.1.0"
//

val scala3Version = "3.1.0"

lazy val commonSettings: Seq[sbt.Def.Setting[_]] = Seq(
  version            := indigoVersion,
  scalaVersion       := scala3Version,
  crossScalaVersions := Seq(scala3Version),
  organization       := "io.indigoengine",
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "0.7.29" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalacOptions ++= Seq("-language:strictEquality"),
  crossScalaVersions := Seq(scala3Version),
  scalafixOnCompile  := true,
  semanticdbEnabled  := true,
  semanticdbVersion  := scalafixSemanticdb.revision,
  autoAPIMappings    := true
)

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo              := sonatypePublishToBundle.value,
    publishMavenStyle      := true,
    sonatypeProfileName    := "io.indigoengine",
    licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "indigo", "indigo@purplekingdomgames.com")),
    developers := List(
      Developer(
        id = "davesmith00000",
        name = "David Smith",
        email = "indigo@purplekingdomgames.com",
        url = url("https://github.com/davesmith00000")
      )
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
      name                := "sandbox",
      showCursor          := true,
      title               := "Sandbox",
      gameAssetsDirectory := "assets"
    )
    .settings(
      publish      := {},
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
      name                := "indigo-perf",
      showCursor          := true,
      title               := "Perf",
      gameAssetsDirectory := "assets",
      windowStartWidth    := 800,
      windowStartHeight   := 600
    )
    .settings(
      publish      := {},
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
    .dependsOn(indigo)
    .settings(
      name                                     := "indigo-extras",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.3" % "test"
    )
    .settings(
      Compile / sourceGenerators += Def.task {
        val cachedFun = FileFunction.cached(
          streams.value.cacheDirectory / "shader-library"
        ) { (files: Set[File]) =>
          ShaderLibraryGen
            .makeShaderLibrary("ExtrasShaderLibrary", "indigoextras.shaders", files, (Compile / sourceManaged).value)
            .toSet
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
    .settings(
      name := "indigo",
      libraryDependencies ++= Seq(
        "org.scalacheck" %%% "scalacheck"                  % "1.15.3" % "test",
        "org.scala-js"   %%% "scalajs-dom"                 % "2.0.0",
        "org.scala-js"   %%% "scala-js-macrotask-executor" % "1.0.0"
      )
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
          ShaderLibraryGen
            .makeShaderLibrary("ShaderLibrary", "indigo.shaders", files, (Compile / sourceManaged).value)
            .toSet
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
        "io.circe" %%% "circe-core"   % "0.14.0-M7",
        "io.circe" %%% "circe-parser" % "0.14.0-M7"
      )
    )
    .dependsOn(indigo)

lazy val indigoShaders =
  project
    .in(file("indigo-shaders"))
    .settings(
      name         := "indigo-shaders",
      version      := indigoVersion,
      scalaVersion := scala3Version,
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "0.7.29" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )

lazy val benchmarks =
  project
    .in(file("benchmarks"))
    .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
    .settings(
      name         := "indigo-benchmarks",
      version      := indigoVersion,
      scalaVersion := scala3Version,
      organization := "io.indigoengine",
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-benchmark" %%% "benchmark" % "0.10.0"
      ),
      jsDependencies ++= Seq(
        "org.webjars" % "chartjs" % "1.0.2" / "Chart.js" minified "Chart.min.js"
      )
    )
    .settings(
      publish      := {},
      publishLocal := {}
    )
    .dependsOn(indigo)
    .dependsOn(indigoExtras)
    .dependsOn(indigoJsonCirce)

lazy val jsdocs = project
  .settings(
    scalaVersion := scala3Version,
    organization := "io.indigoengine"
  )
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0"
  )
  .settings(
    publish      := {},
    publishLocal := {}
  )
  .enablePlugins(ScalaJSPlugin)

lazy val docs = project
  .in(file("indigo-docs"))
  .dependsOn(indigo)
  .dependsOn(indigoExtras)
  .dependsOn(indigoJsonCirce)
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := scala3Version,
    organization := "io.indigoengine"
  )
  .settings(
    mdocVariables := Map(
      "VERSION"         -> indigoDocsVersion,
      "SCALAJS_VERSION" -> scalaJsDocsVersion,
      "SCALA_VERSION"   -> scalaDocsVersion
    ),
    mdocExtraArguments := List("--no-link-hygiene")
  )
  .settings(
    mdocJS := Some(jsdocs)
  )
  .settings(
    publish      := {},
    publishLocal := {}
  )

lazy val rawLogo: String =
    """
    |      //                  //  //
    |                         //
    |    //  //////      //////  //    ////      //////
    |   //  //    //  //    //  //  //    //  //    //
    |  //  //    //  ////////  //  ////////  //////
    |                                   //
    |                            //////
    |""".stripMargin
// Root
lazy val indigoProject =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaUnidocPlugin)
    .settings(commonSettings: _*)
    .settings(
      name                                       := "Indigo",
      ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(indigoShaders, sandbox, perf, docs),
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      }
    )
    .aggregate(
      indigo,
      indigoExtras,
      indigoJsonCirce,
      indigoShaders,
      sandbox,
      perf,
      docs,
      benchmarks
    )
    .settings(
      logo := rawLogo + s"version ${version.value}",
      usefulTasks := Seq(
        UsefulTask("", "cleanAll", "Clean all the projects"),
        UsefulTask("", "buildAllNoClean", "Rebuild without cleaning"),
        UsefulTask("", "testAllNoClean", "Test all without cleaning"),
        UsefulTask("", "crossLocalPublishNoClean", "Locally publish the core modules"),
        UsefulTask("", "gendocs", "Rebuild the API and markdown docs"),
        UsefulTask("", "sandboxRun", "Run the sandbox game (fastOptJS + Electron)"),
        UsefulTask("", "perfRun", "Run the perf game (fastOptJS + Electron)"),
        UsefulTask("", "code", "Launch VSCode")
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.CYAN,
      commandColor     := scala.Console.BLUE_B,
      descriptionColor := scala.Console.WHITE
    )

addCommandAlias(
  "gendocs",
  List(
    "cleanAll",
    "unidoc",   // Docs in ./target/scala-3.1.0/unidoc/
    "docs/mdoc" // Docs in ./indigo/indigo-docs/target/mdoc
  ).mkString(";", ";", "")
)

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
