Global / onChangedBuildSource := ReloadOnSourceChanges

val scala3Version = "3.2.2"

lazy val indigoSite =
  (project in file("."))
    .enablePlugins(GhpagesPlugin)
    .settings(
      name         := "indigo site publisher",
      version      := "0.0.1",
      scalaVersion := scala3Version,
      organization := "io.indigo",
      siteSourceDirectory := target.value / ".." / "build" / "indigo-site",
      makeSite / includeFilter := "*",
      makeSite / excludeFilter := ".DS_Store",
      git.remoteRepo := "git@github.com:PurpleKingdomGames/indigo.git",
      ghpagesNoJekyll := true
    )

addCommandAlias(
  "publishIndigoSite",
  List(
    "makeSite",
    "ghpagesPushSite"
  ).mkString(";", ";", "")
)
