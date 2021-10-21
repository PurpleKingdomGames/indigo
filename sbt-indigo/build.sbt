import xerial.sbt.Sonatype._

lazy val sbtIndigo =
  (project in file("."))
    .settings(
      name := "sbt-indigo",
      sbtPlugin := true,
      version := IndigoVersion.getVersion,
      scalaVersion := "2.12.15", // This is a plugin! Only 2.12 is supported!
      organization := "io.indigoengine",
      scalacOptions ++= Scalac212Options.scala212Compile,
      libraryDependencies ++= Seq(
        "commons-io"       % "commons-io"    % "2.6",
        "io.indigoengine" %% "indigo-plugin" % IndigoVersion.getVersion,
        "com.lihaoyi"     %% "os-lib"        % "0.7.8"
      ),
      publishTo := sonatypePublishToBundle.value,
      publishMavenStyle := true,
      sonatypeProfileName := "io.indigoengine",
      licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
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
