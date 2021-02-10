package com.example.sandbox

import indigo._

object SandboxAssets {

  val smallFontName: AssetName = AssetName("smallFontName")
  val dudeName: AssetName      = AssetName("base_charactor")
  val light: AssetName         = AssetName("light")
  val dots: AssetName          = AssetName("dots")

  val fontMaterial: StandardMaterial.ImageEffects  = StandardMaterial.ImageEffects(smallFontName)
  val lightMaterial: StandardMaterial.ImageEffects = StandardMaterial.ImageEffects(light)
  val dudeMaterial: StandardMaterial.ImageEffects  = StandardMaterial.ImageEffects(dudeName)

  val dotsMaterial: StandardMaterial =
    StandardMaterial
      .ImageEffects(dots)
      .withOverlay(Overlay.LinearGradiant(Point.zero, RGBA.Cyan, Point(32, 32), RGBA.Magenta))
  // .withTint(RGBA.Red)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(smallFontName, AssetPath("assets/boxy_font.png")),
      AssetType.Image(light, AssetPath("assets/light_texture.png")),
      AssetType.Text(AssetName(dudeName.value + "-json"), AssetPath("assets/" + dudeName.value + ".json")),
      AssetType.Image(dudeName, AssetPath("assets/" + dudeName.value + ".png")),
      AssetType.Image(dots, AssetPath("assets/" + dots.value + ".png"))
    )

}

object Fonts {

  val fontKey: FontKey = FontKey("Sandbox font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, 320, 230, FontChar(" ", 145, 52, 23, 23)).isCaseInSensitive
      .addChar(FontChar("A", 3, 78, 23, 23))
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("D", 73, 78, 23, 23))
      .addChar(FontChar("E", 96, 78, 23, 23))
      .addChar(FontChar("F", 119, 78, 23, 23))
      .addChar(FontChar("G", 142, 78, 23, 23))
      .addChar(FontChar("H", 165, 78, 23, 23))
      .addChar(FontChar("I", 188, 78, 15, 23))
      .addChar(FontChar("J", 202, 78, 23, 23))
      .addChar(FontChar("K", 225, 78, 23, 23))
      .addChar(FontChar("L", 248, 78, 23, 23))
      .addChar(FontChar("M", 271, 78, 23, 23))
      .addChar(FontChar("N", 3, 104, 23, 23))
      .addChar(FontChar("O", 29, 104, 23, 23))
      .addChar(FontChar("P", 54, 104, 23, 23))
      .addChar(FontChar("Q", 75, 104, 23, 23))
      .addChar(FontChar("R", 101, 104, 23, 23))
      .addChar(FontChar("S", 124, 104, 23, 23))
      .addChar(FontChar("T", 148, 104, 23, 23))
      .addChar(FontChar("U", 173, 104, 23, 23))
      .addChar(FontChar("V", 197, 104, 23, 23))
      .addChar(FontChar("W", 220, 104, 23, 23))
      .addChar(FontChar("X", 248, 104, 23, 23))
      .addChar(FontChar("Y", 271, 104, 23, 23))
      .addChar(FontChar("Z", 297, 104, 23, 23))
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
      .addChar(FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))
      .addChar(FontChar(".", 286, 0, 15, 23))
      .addChar(FontChar(",", 248, 0, 15, 23))
      .addChar(FontChar(" ", 145, 52, 23, 23))

}
