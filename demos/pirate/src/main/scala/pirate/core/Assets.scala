package pirate.core

import indigo.*
import pirate.generated.Assets.*

object Assets:

  object Static:
    val chestGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 64, 35), 4, assets.static.ChestClose01Material).withRef(33, 34)

  object Clouds:

    val bigCloudsGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 448, 101), 40, assets.clouds.BigCloudsMaterial).withRef(0, 101)
    val bigCloudsWidth: Int = bigCloudsGraphic.crop.width

    val cloud1: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 140, 39), 45, assets.clouds.smallCloudsMaterial)
    val cloud2: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 39, 140, 39), 45, assets.clouds.smallCloudsMaterial)
    val cloud3: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 78, 140, 39), 45, assets.clouds.smallCloudsMaterial)

  object Trees:

    val tallTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, assets.trees.FrontPalmTreesMaterial)
        .withCrop(Rectangle(8, 0, 16, 60))

    val leftLeaningTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, assets.trees.FrontPalmTreesMaterial)
        .withCrop(Rectangle(43, 0, 50, 22))

    val rightLeaningTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, assets.trees.FrontPalmTreesMaterial)
        .withCrop(Rectangle(36, 32, 48, 23))

  object Fonts:
    val fontKey: FontKey                    = FontKey("boxy font")
    val fontMaterial: Material.ImageEffects = Material.ImageEffects(assets.fonts.boxyFontSmall)

    val fontInfo: FontInfo =
      FontInfo(fontKey, 320, 230, FontChar("?", 47, 26, 11, 12))
        .addChar(FontChar("A", 2, 39, 10, 12))
        .addChar(FontChar("B", 14, 39, 9, 12))
        .addChar(FontChar("C", 25, 39, 10, 12))
        .addChar(FontChar("D", 37, 39, 9, 12))
        .addChar(FontChar("E", 49, 39, 9, 12))
        .addChar(FontChar("F", 60, 39, 9, 12))
        .addChar(FontChar("G", 72, 39, 9, 12))
        .addChar(FontChar("H", 83, 39, 9, 12))
        .addChar(FontChar("I", 95, 39, 5, 12))
        .addChar(FontChar("J", 102, 39, 9, 12))
        .addChar(FontChar("K", 113, 39, 10, 12))
        .addChar(FontChar("L", 125, 39, 9, 12))
        .addChar(FontChar("M", 136, 39, 13, 12))
        .addChar(FontChar("N", 2, 52, 11, 12))
        .addChar(FontChar("O", 15, 52, 10, 12))
        .addChar(FontChar("P", 27, 52, 9, 12))
        .addChar(FontChar("Q", 38, 52, 11, 12))
        .addChar(FontChar("R", 51, 52, 10, 12))
        .addChar(FontChar("S", 63, 52, 9, 12))
        .addChar(FontChar("T", 74, 52, 11, 12))
        .addChar(FontChar("U", 87, 52, 10, 12))
        .addChar(FontChar("V", 99, 52, 9, 12))
        .addChar(FontChar("W", 110, 52, 13, 12))
        .addChar(FontChar("X", 125, 52, 9, 12))
        .addChar(FontChar("Y", 136, 52, 11, 12))
        .addChar(FontChar("Z", 149, 52, 10, 12))
        .addChar(FontChar("0", 2, 13, 10, 12))
        .addChar(FontChar("1", 13, 13, 7, 12))
        .addChar(FontChar("2", 21, 13, 9, 12))
        .addChar(FontChar("3", 33, 13, 9, 12))
        .addChar(FontChar("4", 44, 13, 9, 12))
        .addChar(FontChar("5", 56, 13, 9, 12))
        .addChar(FontChar("6", 67, 13, 9, 12))
        .addChar(FontChar("7", 79, 13, 9, 12))
        .addChar(FontChar("8", 90, 13, 10, 12))
        .addChar(FontChar("9", 102, 13, 9, 12))
        .addChar(FontChar("?", 47, 26, 11, 12))
        .addChar(FontChar("!", 2, 0, 6, 12))
        .addChar(FontChar(".", 143, 0, 6, 12))
        .addChar(FontChar(",", 124, 0, 8, 12))
        .addChar(FontChar("-", 133, 0, 9, 12))
        .addChar(FontChar(" ", 112, 13, 8, 12))
        .addChar(FontChar("[", 2, 65, 7, 12))
        .addChar(FontChar("]", 21, 65, 7, 12))
        .addChar(FontChar("(", 84, 0, 7, 12))
        .addChar(FontChar(")", 93, 0, 7, 12))
        .addChar(FontChar("\\", 11, 65, 8, 12))
        .addChar(FontChar("/", 150, 0, 9, 12))
        .addChar(FontChar(":", 2, 26, 5, 12))
        .addChar(FontChar("@", 60, 26, 11, 12))
        .addChar(FontChar("_", 42, 65, 9, 12))
        .addChar(FontChar("%", 47, 0, 14, 12))

  def initialAssets(baseUrl: String): Set[AssetType] =
    assets.fonts.assetSet(baseUrl) ++
      assets.captain.assetSet(baseUrl)

  def remainingAssets(baseUrl: String): Set[AssetType] =
    assets.static.assetSet(baseUrl) ++
      assets.sounds.assetSet(baseUrl) ++
      assets.clouds.assetSet(baseUrl) ++
      assets.water.assetSet(baseUrl) ++
      assets.flag.assetSet(baseUrl) ++
      assets.trees.assetSet(baseUrl) ++
      assets.helm.assetSet(baseUrl)
