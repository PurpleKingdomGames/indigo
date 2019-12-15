package indigoexamples

import indigo._

object Assets {

  val assets: Set[AssetType] =
    Set(
      AssetType.Image("graphics", "assets/graphics.png"),
      AssetType.Image(FontDetails.fontName, "assets/boxy_font_small.png")
    )

  val cross: Graphic =
    Graphic(0, 0, 3, 3, 1, "graphics")

}
