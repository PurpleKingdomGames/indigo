package com.example.perf

import indigo._

object PerfAssets {

  val smallFontName: AssetName = AssetName("smallFontName")
  val dudeName: AssetName      = AssetName("base_charactor")
  val light: AssetName         = AssetName("light")

  val smallFontNameMaterial: StandardMaterial.Blit = StandardMaterial.Blit(smallFontName)
  val lightMaterial: StandardMaterial.Blit         = StandardMaterial.Blit(light)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(smallFontName, AssetPath("assets/boxy_font.png")),
      AssetType.Image(light, AssetPath("assets/light_texture.png")),
      AssetType.Text(AssetName(dudeName.value + "-json"), AssetPath("assets/" + dudeName.value + ".json")),
      AssetType.Image(dudeName, AssetPath("assets/" + dudeName.value + ".png"))
    )

}
