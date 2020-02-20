package indigoexamples

import indigo._

object Assets {

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")),
      AssetType.Image(FontDetails.fontName, AssetPath("assets/boxy_font_small.png"))
    )

  val cross: Graphic =
    Graphic(0, 0, 3, 3, 1, Material.Textured(AssetName("graphics")))

}
