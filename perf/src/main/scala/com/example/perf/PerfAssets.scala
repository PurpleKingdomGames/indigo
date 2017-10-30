package com.example.perf

import com.purplekingdomgames.shared.{AssetType, ImageAsset, TextAsset}

object PerfAssets {

  val smallFontName: String = "smallFontName"
  val dudeName: String = "base_charactor"
  val light: String = "light"

  def assets: Set[AssetType] =
    Set(
      ImageAsset(smallFontName, "assets/boxy_font.png"),
      ImageAsset(light, "assets/light_texture.png"),
      TextAsset(dudeName + "-json", "assets/" + dudeName + ".json"),
      ImageAsset(dudeName, "assets/" + dudeName + ".png")
    )

}
