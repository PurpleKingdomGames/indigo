package com.example.sandbox

import indigo._

object SandboxAssets {

  val smallFontName: AssetName = AssetName("smallFontName")
  val dudeName: AssetName      = AssetName("base_charactor")
  val light: AssetName         = AssetName("light")
  val dots: AssetName          = AssetName("dots")

  val fontMaterial: Material.ImageEffects  = Material.ImageEffects(smallFontName)
  val lightMaterial: Material.ImageEffects = Material.ImageEffects(light)
  val dudeMaterial: Material.ImageEffects  = Material.ImageEffects(dudeName)

  val dotsMaterial: Material =
    Material
      .ImageEffects(dots)
      // .withOverlay(Overlay.Color(RGBA.Magenta.withAlpha(0.75)))
      // .withOverlay(Overlay.LinearGradient(Point.zero, RGBA.Cyan, Point(32, 32), RGBA.Magenta))
      .withOverlay(Fill.RadialGradient(Point(4, 4), RGBA.Cyan, Point(32, 32), RGBA.Magenta))

  val junctionBoxAlbedo: AssetName   = AssetName("junctionbox_albedo")
  val junctionBoxEmission: AssetName = AssetName("junctionbox_emission")
  val junctionBoxNormal: AssetName   = AssetName("junctionbox_normal")
  val junctionBoxSpecular: AssetName = AssetName("junctionbox_specular")
  val imageLightName: AssetName      = AssetName("light_texture")
  val foliageName: AssetName         = AssetName("foliage")
  val smoothBumpName: AssetName      = AssetName("smooth-bump2")
  val normalMapName: AssetName       = AssetName("normal-map")
  val trafficLightsName: AssetName   = AssetName("trafficlights")

  val junctionBoxMaterial: Material.Bitmap =
    Material.Bitmap(junctionBoxAlbedo)

  val foliageMaterial: Material.Bitmap =
    Material.Bitmap(foliageName)

  val imageLightMaterial: Material.Bitmap =
    Material.Bitmap(imageLightName)

  val smoothBumpMaterial: Material.Bitmap =
    Material.Bitmap(smoothBumpName)

  val normalMapMaterial: Material.Refraction =
    Material.Refraction(normalMapName)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(smallFontName, AssetPath("assets/boxy_font.png")),
      AssetType.Image(light, AssetPath("assets/light_texture.png")),
      AssetType.Text(AssetName(dudeName.value + "-json"), AssetPath("assets/" + dudeName.value + ".json")),
      AssetType.Image(dudeName, AssetPath("assets/" + dudeName.value + ".png")),
      AssetType.Image(dots, AssetPath("assets/" + dots.value + ".png")),
      AssetType.Tagged("atlas1")(
        AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
        AssetType.Image(junctionBoxEmission, AssetPath("assets/" + junctionBoxEmission.value + ".png")),
        AssetType.Image(junctionBoxNormal, AssetPath("assets/" + junctionBoxNormal.value + ".png")),
        AssetType.Image(junctionBoxSpecular, AssetPath("assets/" + junctionBoxSpecular.value + ".png")),
        AssetType.Image(imageLightName, AssetPath("assets/" + imageLightName.value + ".png")),
        AssetType.Image(foliageName, AssetPath("assets/" + foliageName.value + ".png")),
        AssetType.Image(smoothBumpName, AssetPath("assets/" + smoothBumpName.value + ".png")),
        AssetType.Image(normalMapName, AssetPath("assets/" + normalMapName.value + ".png"))
      ),
      AssetType.Image(trafficLightsName, AssetPath("assets/" + trafficLightsName.value + ".png"))
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
