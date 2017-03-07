package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.{AssetType, ImageAsset, TextAsset}

object MyAssets {

  val spriteSheetName1: String = "blob1"
  val spriteSheetName2: String = "blob2"
  val spriteSheetName3: String = "f"
  val trafficLightsName: String = "trafficlights"
  val fontName: String = "fontName"
  val dudeName: String = "base_charactor"
  val sludge: String = "sludge"

  private val spriteAsset1 = ImageAsset(spriteSheetName1, "Sprite-0001.png")
  private val spriteAsset2 = ImageAsset(spriteSheetName2, "Sprite-0002.png")
  private val spriteAsset3 = ImageAsset(spriteSheetName3, "f-texture.png")
  private val trafficLightsAsset = ImageAsset(trafficLightsName, "trafficlights.png")
  private val fontAsset = ImageAsset(fontName, "boxy_bold_font_5.png")

  def assets: Set[AssetType] =
    Set(
      spriteAsset1,
      spriteAsset2,
      spriteAsset3,
      trafficLightsAsset,
      fontAsset,
      TextAsset(trafficLightsName + "-json", trafficLightsName + ".json"),
      TextAsset(dudeName + "-json", dudeName + ".json"),
      ImageAsset(dudeName, dudeName + ".png"),
      ImageAsset(sludge, "sludge.png")
    )

}
