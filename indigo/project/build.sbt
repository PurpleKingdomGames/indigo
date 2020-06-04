lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

lazy val publishSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishTo := sonatypePublishToBundle.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ =>
      false
    },
    pomExtra :=
      <url>https://github.com/PurpleKingdomGames/indigo</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>https://opensource.org/licenses/MIT</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <developers>
        <developer>
          <id>davesmith00000</id>
          <name>David Smith</name>
          <organization>Purple Kingdom Games</organization>
          <organizationUrl>http://purplekingdomgames.com/</organizationUrl>
        </developer>
      </developers>,
    sonatypeProfileName := "Purple Kingdom Game",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    sonatypeProjectHosting := Some(GitHubHosting("PurpleKingdomGames", "indigo", "indigo@purplekingdomgames.com")),
    homepage := Some(url("https://github.com/PurpleKingdomGames/indigo")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/PurpleKingdomGames/indigo"),
        "scm:git@github.com:PurpleKingdomGames/indigo.git"
      )
    ),
    developers := List(
      Developer(id = "davesmith00000", name = "David Smith", email = "indigo@purplekingdomgames.com", url = url("https://github.com/davesmith00000"))
    )
  )
}

// Plugin
lazy val sbtIndigo =
  (project in file("sbt-indigo"))
    .settings(publishSettings: _*)
    .settings(
      version := sbtIndigoVersion,
      scalaVersion := "2.12.10", // This is a plugin! Only 2.12 is supported!
      organization := "io.indigoengine",
      scalacOptions ++= Scalac212Options.scala212Compile
    )
    .settings(
      name := "sbt-indigo",
      sbtPlugin := true,
      libraryDependencies ++= Seq(
        "commons-io" % "commons-io" % "2.6"
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % "0.13.0"),
      unmanagedSourceDirectories in Compile :=
        mirrorScalaSource((baseDirectory in ThisBuild).value.getParentFile / "sbt-indigo")
    )
    .enablePlugins(ScalaJSPlugin)

def mirrorScalaSource(baseDirectory: File): Seq[File] = {
  val scalaSourceDir = baseDirectory / "src" / "main" / "scala"
  if (scalaSourceDir.exists) scalaSourceDir :: Nil
  else sys.error(s"Missing source directory: $scalaSourceDir")
}
