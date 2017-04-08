package com.example.sandbox

import com.purplekingdomgames.indigo.gameengine.assets.{AssetType, ImageAsset, TextAsset}

object MyAssets {

  val smallFontName: String = "smallFontName"
  val dudeName: String = "base_charactor"
  val light: String = "light"

  def assets: Set[AssetType] =
    Set(
      ImageAsset(smallFontName, "boxy_font.png"),
      ImageAsset(light, "light_texture.png"),
      TextAsset(dudeName + "-json", dudeName + ".json"),
      ImageAsset(dudeName, dudeName + ".png")
    )

}
