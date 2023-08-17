import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting

ThisBuild / scalaVersion := "3.3.0"

enablePlugins(LaikaPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges

Laika / sourceDirectories := Seq(baseDirectory.value / "../indigo/indigo-docs/target/mdoc")

laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting)
