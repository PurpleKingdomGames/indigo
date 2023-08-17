import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.rewrite.nav.PrettyURLs
import laika.helium.Helium

import laika.helium.config._
import laika.ast.Path.Root

ThisBuild / scalaVersion := "3.3.0"

enablePlugins(LaikaPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges

Laika / sourceDirectories := Seq(
  baseDirectory.value / "../indigo/indigo-docs/target/mdoc"
)

laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting, PrettyURLs)

laikaTheme :=
  Helium.defaults.all
    .metadata(
      title = Some("Indigo"),
      language = Some("en")
    )
    .site
    .topNavigationBar(
      homeLink = IconLink.internal(Root / "README.md", HeliumIcon.home),
      navLinks = Seq(
        TextLink.external("http://somewhere.com/", "Text Link"),
        ButtonLink.external("http://somewhere.com/", "Button Link")
      )
    )
    .site.tableOfContent(title = "Contents", depth = 2)
    // .site.downloadPage(
    //   title = "Documentation Download",
    //   description = Some("Optional Text Below Title"),
    //   downloadPath = Root / "downloads",
    //   includeEPUB = true,
    //   includePDF = true
    // )
    .build

// Helium.defaults
