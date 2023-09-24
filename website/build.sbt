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
import com.comcast.ip4s._
import scala.concurrent.duration.DurationInt
import laika.sbt.LaikaPreviewConfig
import java.time.OffsetDateTime

ThisBuild / scalaVersion := "3.3.1"

enablePlugins(LaikaPlugin, GhpagesPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges

Laika / sourceDirectories := Seq(
  baseDirectory.value / "../indigo/indigo-docs/target/mdoc"
)

laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting, PrettyURLs)

/*
Dark purple - #29016a
hot pink - #ae2be2 / less hot: 9003c8
light pink - #efc6ff
Purpleish - 6237a7
Light grey - e4e4e4
Light purple - a888db
 */

laikaTheme :=
  Helium.defaults.all
    .metadata(
      title = Some("Indigo"),
      description = Some("A 2D Pixel Art FP game engine for Scala."),
      identifier = Some(""),
      authors = Seq(),
      language = Some("en"),
      datePublished = Some(OffsetDateTime.now),
      version = Some("1.0.0")
    )
    .all
    .themeColors(
      primary = Color.hex("29016a"),
      secondary = Color.hex("9003c8"),
      primaryMedium = Color.hex("a888db"),
      primaryLight = Color.hex("e4e4e4"),
      text = Color.hex("5f5f5f"),
      background = Color.hex("ffffff"),
      bgGradient = (Color.hex("095269"), Color.hex("007c99"))
    )
    .site
    .darkMode
    .themeColors(
      primary = Color.hex("29016a"),
      secondary = Color.hex("9003c8"),
      primaryMedium = Color.hex("a888db"),
      primaryLight = Color.hex("e4e4e4"),
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
          height = Some(Length(50.0, LengthUnit.px))
        )
      ),
      navLinks = Seq(
        ButtonLink.external("https://discord.gg/b5CD47g", "Discord"),
        ButtonLink.external("/api", "API"),
        ButtonLink.external(
          "https://github.com/PurpleKingdomGames/indigo",
          "Github"
        )
      )
    )
    .site
    .tableOfContent(title = "Contents", depth = 2)
    .site
    .autoLinkCSS(Root / "css" / "custom.css")
    .site.favIcons(
      Favicon.internal(Root / "img" / "indigo_logo_solid.svg", sizes = "32x32"),
      Favicon.internal(Root / "img" / "indigo_logo_solid.svg", sizes = "64x64")
    )
    .build

// Helium.defaults

laikaPreviewConfig :=
  LaikaPreviewConfig.defaults
    .withPort(port"8080")

// Make site

siteSourceDirectory := target.value / "docs" / "site"
makeSite / includeFilter := "*"
makeSite / excludeFilter := ".DS_Store"
git.remoteRepo := "git@github.com:PurpleKingdomGames/indigo.git"
ghpagesNoJekyll := true

addCommandAlias(
  "publishIndigoSite",
  List(
    "makeSite",
    "ghpagesPushSite"
  ).mkString(";", ";", "")
)
