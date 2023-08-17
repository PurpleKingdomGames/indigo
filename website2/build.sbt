import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.rewrite.nav.PrettyURLs
import laika.helium.Helium
import laika.theme.config.Color
import laika.theme.config.Color._
import laika.helium.config.ColorQuintet
import laika.helium.config._
import laika.ast.Path.Root
import laika.ast.Image
import laika.ast.Length
import laika.ast.LengthUnit

ThisBuild / scalaVersion := "3.3.0"

enablePlugins(LaikaPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges

Laika / sourceDirectories := Seq(
  baseDirectory.value / "../indigo/indigo-docs/target/mdoc"
)

laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting, PrettyURLs)

/*
Dark purple - #29016a
hot pink - #ae2be2
light pink - #efc6ff
Purpleish
 */

laikaTheme :=
  Helium.defaults.all
    .metadata(
      title = Some("Indigo"),
      language = Some("en")
    )
    .all
    .themeColors(
      primary = Color.hex("ffffff"),
      secondary = Color.hex("ae2be2"),
      primaryMedium = Color.hex("6237a7"),
      primaryLight = Color.hex("29016a"),
      text = Color.hex("5f5f5f"),
      background = Color.hex("ffffff"),
      bgGradient = (Color.hex("095269"), Color.hex("007c99"))
    )
    .site
    .darkMode
    .themeColors(
      primary = Color.hex("ffffff"),
      secondary = Color.hex("ae2be2"),
      primaryMedium = Color.hex("6237a7"),
      primaryLight = Color.hex("29016a"),
      text = Color.hex("5f5f5f"),
      background = Color.hex("ffffff"),
      bgGradient = (Color.hex("29016a"), Color.hex("ffffff"))
    )
    .all
    .syntaxHighlightingColors(
      base = ColorQuintet(
        hex("2a3236"),
        hex("8c878e"),
        hex("b2adb4"),
        hex("bddcee"),
        hex("e8e8e8")
      ),
      wheel = ColorQuintet(
        hex("e28e93"),
        hex("ef9725"),
        hex("ffc66d"),
        hex("7fb971"),
        hex("4dbed4")
      )
    )
    .site
    .topNavigationBar(
      homeLink = ImageLink.internal(
        Root / "README.md",
        Image.internal(
          Root / "img" / "indigo_logo_solid_text.svg",
          alt = Some("Homepage"),
          title = Some("Indigo"),
          width = Some(Length(150.0, LengthUnit.px)),
          height = Some(Length(50.0, LengthUnit.px)),
        )
      ),
      navLinks = Seq(
        TextLink.external("http://somewhere.com/", "Text Link"),
        ButtonLink.external("http://somewhere.com/", "Button Link")
      )
    )
    .site
    .tableOfContent(title = "Contents", depth = 2)
    // .site.downloadPage(
    //   title = "Documentation Download",
    //   description = Some("Optional Text Below Title"),
    //   downloadPath = Root / "downloads",
    //   includeEPUB = true,
    //   includePDF = true
    // )
    .build

// Helium.defaults

import com.comcast.ip4s._
import scala.concurrent.duration.DurationInt
import laika.sbt.LaikaPreviewConfig

laikaPreviewConfig :=
  LaikaPreviewConfig.defaults
    .withPort(port"8080")
