import xerial.sbt.Sonatype._

lazy val sbtIndigo =
  (project in file("."))
    .settings(
      name         := "sbt-indigo",
      sbtPlugin    := true,
      version      := IndigoVersion.getVersion,
      scalaVersion := "2.12.20", // This is a plugin! Only 2.12 is supported!
      scalacOptions ++= Scalac212Options.scala212Compile,
      libraryDependencies ++= Seq(
        "commons-io"       % "commons-io"    % "2.6",
        "io.indigoengine" %% "indigo-plugin" % IndigoVersion.getVersion,
        "com.lihaoyi"     %% "os-lib"        % "0.11.3"
      )
    )
    .settings(publishSettings)

lazy val publishSettings =
  Seq(
    organization         := "io.indigoengine",
    organizationName     := "PurpleKingdomGames",
    organizationHomepage := Some(url("https://purplekingdomgames.com/")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/PurpleKingdomGames/indigo"),
        "scm:git@github.com:PurpleKingdomGames/indigo.git"
      )
    ),
    developers := List(
      Developer(
        id = "davesmith00000",
        name = "David Smith",
        email = "indigo@purplekingdomgames.com",
        url = url("https://github.com/davesmith00000")
      )
    ),
    description := "Indigo's sbt plugin, providing utilities for making games with Indigo.",
    licenses := List(
      "MIT" -> url("https://opensource.org/licenses/MIT")
    ),
    homepage := Some(url("https://github.com/PurpleKingdomGames/indigo")),

    // Remove all additional repository other than Maven Central from POM
    pomIncludeRepository := { _ => false },
    publishMavenStyle    := true,

    // new setting for the Central Portal
    publishTo := {
      val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
      if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
      else localStaging.value
    }
  )