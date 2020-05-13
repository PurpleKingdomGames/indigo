package pirate.init

import indigo._

object Assets {

  object Static {
    val backgroundRef: AssetName = AssetName("background")

    val levelRef: AssetName   = AssetName("level")
    val levelGraphic: Graphic = Graphic(Rectangle(0, 0, 646, 374), 2, Material.Textured(levelRef))

    val chestRef: AssetName   = AssetName("Chest Close 01")
    val chestGraphic: Graphic = Graphic(Rectangle(0, 0, 64, 35), 4, Material.Textured(chestRef)).withRef(33, 34)

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Static.backgroundRef, AssetPath("assets/bg.png")),
        AssetType.Image(Static.chestRef, AssetPath("assets/" + Static.chestRef.value + ".png")),
        AssetType.Image(Static.levelRef, AssetPath("assets/level.png"))
      )
  }

  object Sounds {
    val shanty: AssetName       = AssetName("shanty")
    val walkSound: AssetName    = AssetName("walk")
    val respawnSound: AssetName = AssetName("respawn")

    val assets: Set[AssetType] =
      Set(
        AssetType.Audio(Sounds.shanty, AssetPath("assets/bgmusic.mp3")),
        AssetType.Audio(Sounds.walkSound, AssetPath("assets/walk.mp3")),
        AssetType.Audio(Sounds.respawnSound, AssetPath("assets/respawn.mp3"))
      )
  }

  object Clouds {

    val bigCloudsRef: AssetName   = AssetName("Big Clouds")
    val smallCloudsRef: AssetName = AssetName("small_clouds")

    val bigCloudsGraphic: Graphic = Graphic(Rectangle(0, 0, 448, 101), 40, Material.Textured(bigCloudsRef)).withRef(0, 101)
    val bigCloudsWidth: Int       = bigCloudsGraphic.bounds.width

    val animationKey: AnimationKey = AnimationKey("cloud")

    val cloudsAnimation: Animation =
      Animation(
        animationKey,
        Material.Textured(smallCloudsRef),
        Frame.fromBounds(0, 0, 140, 39),
        Frame.fromBounds(0, 39, 140, 39),
        Frame.fromBounds(0, 78, 140, 39)
      )

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Clouds.bigCloudsRef, AssetPath("assets/" + Clouds.bigCloudsRef.value + ".png")),
        AssetType.Image(Clouds.smallCloudsRef, AssetPath("assets/" + Clouds.smallCloudsRef.value + ".png"))
      )

  }

  object Water {
    val ref: AssetName     = AssetName("Water Reflect")
    val jsonRef: AssetName = AssetName("Water Reflect JSON")

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Water.ref, AssetPath("assets/" + Water.ref.value + ".png")),
        AssetType.Text(Water.jsonRef, AssetPath("assets/" + Water.ref.value + ".json"))
      )
  }

  object Flag {
    val ref: AssetName     = AssetName("Flag")
    val jsonRef: AssetName = AssetName("Flag JSON")

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Flag.ref, AssetPath("assets/" + Flag.ref.value + ".png")),
        AssetType.Text(Flag.jsonRef, AssetPath("assets/" + Flag.ref.value + ".json"))
      )
  }

  object Captain {
    val ref: AssetName     = AssetName("Captain Clown Nose")
    val jsonRef: AssetName = AssetName("Captain Clown Nose JSON")

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Captain.ref, AssetPath("assets/" + Captain.ref.value + ".png")),
        AssetType.Text(Captain.jsonRef, AssetPath("assets/" + Captain.ref.value + ".json"))
      )
  }

  object Trees {
    val ref: AssetName     = AssetName("Palm Tree")
    val jsonRef: AssetName = AssetName("Palm Tree JSON")

    val trunksRef: AssetName = AssetName("Front Palm Trees")

    val tallTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Textured(trunksRef))
        .withCrop(Rectangle(8, 0, 16, 60))

    val leftLeaningTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Textured(trunksRef))
        .withCrop(Rectangle(43, 0, 50, 22))

    val rightLeaningTrunkGraphic: Graphic =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Textured(trunksRef))
        .withCrop(Rectangle(36, 32, 48, 23))

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Trees.trunksRef, AssetPath("assets/" + Trees.trunksRef.value + ".png")),
        AssetType.Image(Trees.ref, AssetPath("assets/" + Trees.ref.value + ".png")),
        AssetType.Text(Trees.jsonRef, AssetPath("assets/" + Trees.ref.value + ".json"))
      )
  }

  object Helm {
    val ref: AssetName     = AssetName("Ship Helm")
    val jsonRef: AssetName = AssetName("Ship Helm JSON")

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Helm.ref, AssetPath("assets/" + Helm.ref.value + ".png")),
        AssetType.Text(Helm.jsonRef, AssetPath("assets/" + Helm.ref.value + ".json"))
      )
  }

  object Fonts {
    val smallFontName: AssetName = AssetName("smallFontName")
    val fontKey: FontKey         = FontKey("boxy font")

    val fontInfo: FontInfo =
      FontInfo(fontKey, Material.Textured(smallFontName), 320, 230, FontChar("?", 47, 26, 11, 12))
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
        AssetType.Image(Fonts.smallFontName, AssetPath("assets/boxy_font_small.png"))
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
