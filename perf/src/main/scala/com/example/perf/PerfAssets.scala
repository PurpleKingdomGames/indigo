package com.example.perf

import com.purplekingdomgames.shared.AssetType

object PerfAssets {

  val smallFontName: String = "smallFontName"
  val dudeName: String      = "base_charactor"
  val light: String         = "light"

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(smallFontName, "assets/boxy_font.png"),
      AssetType.Image(light, "assets/light_texture.png"),
      AssetType.Text(dudeName + "-json", "assets/" + dudeName + ".json"),
      AssetType.Image(dudeName, "assets/" + dudeName + ".png")
    )

}
