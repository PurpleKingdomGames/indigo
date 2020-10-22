package com.example.jobs

import indigo._

object Assets {

  val dots: AssetName = AssetName("dots")
  val font: AssetName = AssetName("boxy font")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(dots, AssetPath("assets/dots.png")),
      AssetType.Image(font, AssetPath("assets/boxy_font.png"))
    )

  val redDot: Graphic    = Graphic(Rectangle(0, 0, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val greenDot: Graphic  = Graphic(Rectangle(16, 0, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val blueDot: Graphic   = Graphic(Rectangle(0, 16, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)
  val yellowDot: Graphic = Graphic(Rectangle(16, 16, 16, 16), 1, Material.Textured(dots)).withRef(8, 8)

  val fontKey: FontKey = FontKey("Game font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, Material.Textured(font), 320, 230, FontChar(" ", 145, 52, 23, 23)).isCaseInSensitive
      .addChar(FontChar("0", 3, 26, 23, 23))
      .addChar(FontChar("1", 26, 26, 15, 23))
      .addChar(FontChar("2", 41, 26, 23, 23))
      .addChar(FontChar("3", 64, 26, 23, 23))
      .addChar(FontChar("4", 87, 26, 23, 23))
      .addChar(FontChar("5", 110, 26, 23, 23))
      .addChar(FontChar("6", 133, 26, 23, 23))
      .addChar(FontChar("7", 156, 26, 23, 23))
      .addChar(FontChar("8", 179, 26, 23, 23))
      .addChar(FontChar("9", 202, 26, 23, 23))
}
