package pirate

import indigo._

object Assets {

  object Static {
    val backgroundRef: String      = "background"
    val backgroundGraphic: Graphic = Graphic(Rectangle(0, 0, 640, 360), 50, backgroundRef)

    val levelRef: String      = "level"
    val levelGraphic: Graphic = Graphic(Rectangle(0, 0, 646, 374), 2, levelRef)

    val chestRef: String      = "Chest Close 01"
    val chestGraphic: Graphic = Graphic(Rectangle(0, 0, 64, 35), 4, chestRef).withRef(33, 34)

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Static.backgroundRef, "assets/bg.png"),
        AssetType.Image(Static.chestRef, "assets/" + Static.chestRef + ".png"),
        AssetType.Image(Static.levelRef, "assets/level.png")
      )
  }

  object Sounds {
    val shanty: String       = "shanty"
    val walkSound: String    = "walk"
    val respawnSound: String = "respawn"

    val assets: Set[AssetType] =
      Set(
        AssetType.Audio(Sounds.shanty, "assets/bgmusic.mp3"),
        AssetType.Audio(Sounds.walkSound, "assets/walk.mp3"),
        AssetType.Audio(Sounds.respawnSound, "assets/respawn.mp3")
      )
  }

  object Clouds {

    val bigCloudsRef: String   = "Big Clouds"
    val smallCloud1Ref: String = "Small Cloud 1"
    val smallCloud2Ref: String = "Small Cloud 2"
    val smallCloud3Ref: String = "Small Cloud 3"

    val bigCloudsGraphic: Graphic = Graphic(Rectangle(0, 0, 448, 101), 40, bigCloudsRef).withRef(0, 101)
    val bigCloudsWidth: Int       = bigCloudsGraphic.bounds.width

    val cloudGraphic1: Graphic = Graphic(Rectangle(0, 0, 74, 24), 30, smallCloud1Ref)
    val cloudGraphic2: Graphic = Graphic(Rectangle(0, 0, 133, 35), 30, smallCloud2Ref)
    val cloudGraphic3: Graphic = Graphic(Rectangle(0, 0, 140, 39), 30, smallCloud3Ref)

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Clouds.bigCloudsRef, "assets/" + Clouds.bigCloudsRef + ".png"),
        AssetType.Image(Clouds.smallCloud1Ref, "assets/" + Clouds.smallCloud1Ref + ".png"),
        AssetType.Image(Clouds.smallCloud2Ref, "assets/" + Clouds.smallCloud2Ref + ".png"),
        AssetType.Image(Clouds.smallCloud3Ref, "assets/" + Clouds.smallCloud3Ref + ".png")
      )

  }

  object Water {
    val ref: String     = "Water Reflect"
    val jsonRef: String = "Water Reflect JSON"

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Water.ref, "assets/" + Water.ref + ".png"),
        AssetType.Text(Water.jsonRef, "assets/" + Water.ref + ".json")
      )
  }

  object Flag {
    val ref: String     = "Flag"
    val jsonRef: String = "Flag JSON"

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Flag.ref, "assets/" + Flag.ref + ".png"),
        AssetType.Text(Flag.jsonRef, "assets/" + Flag.ref + ".json")
      )
  }

  object Captain {
    val ref: String     = "Captain Clown Nose"
    val jsonRef: String = "Captain Clown Nose JSON"

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Captain.ref, "assets/" + Captain.ref + ".png"),
        AssetType.Text(Captain.jsonRef, "assets/" + Captain.ref + ".json")
      )
  }

  object Trees {
    val ref: String     = "Palm Tree"
    val jsonRef: String = "Palm Tree JSON"

    val trunksRef: String = "Front Palm Trees"

    val tallTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, trunksRef)
        .withCrop(Rectangle(8, 0, 16, 60))

    val leftLeaningTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, trunksRef)
        .withCrop(Rectangle(43, 0, 50, 22))

    val rightLeaningTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, trunksRef)
        .withCrop(Rectangle(36, 32, 48, 23))

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Trees.trunksRef, "assets/" + Trees.trunksRef + ".png"),
        AssetType.Image(Trees.ref, "assets/" + Trees.ref + ".png"),
        AssetType.Text(Trees.jsonRef, "assets/" + Trees.ref + ".json")
      )
  }

  object Helm {
    val ref: String     = "Ship Helm"
    val jsonRef: String = "Ship Helm JSON"

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Helm.ref, "assets/" + Helm.ref + ".png"),
        AssetType.Text(Helm.jsonRef, "assets/" + Helm.ref + ".json")
      )
  }

  object Fonts {
    val smallFontName: String = "smallFontName"
    val fontKey: FontKey      = FontKey("boxy font")

    val fontInfo: FontInfo =
      FontInfo(fontKey, smallFontName, 320, 230, FontChar("?", 47, 26, 11, 12))
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

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Fonts.smallFontName, "assets/boxy_font_small.png")
      )
  }

  val assets: Set[AssetType] =
    Static.assets ++
      Sounds.assets ++
      Clouds.assets ++
      Water.assets ++
      Flag.assets ++
      Captain.assets ++
      Trees.assets ++
      Helm.assets ++
      Fonts.assets
}
