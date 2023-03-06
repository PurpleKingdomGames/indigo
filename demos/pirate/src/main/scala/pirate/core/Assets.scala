package pirate.core

import indigo.*

/*
This object declares all of the assets we want to load statically.
You can do this dynamically too if you like, e.g. load a text file
that lists the assets to be loaded. No need in this case.

We also declare information about fonts, graphics, and animations here.
It doesn't matter that none of their assets have loaded yet.
 */
object Assets:

  object Static:
    val backgroundRef: AssetName = AssetName("background")

    val terrainJsonRef: AssetName = AssetName("terrainJson")
    val terrainRef: AssetName     = AssetName("terrain")

    val chestRef: AssetName = AssetName("Chest Close 01")
    val chestGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 64, 35), 4, Material.Bitmap(chestRef)).withRef(33, 34)

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Static.backgroundRef, AssetPath(baseUrl + "assets/bg.png")),
        AssetType.Image(Static.chestRef, AssetPath(baseUrl + "assets/" + Static.chestRef + ".png")),
        AssetType.Image(Static.terrainRef, AssetPath(baseUrl + "assets/terrain.png")),
        AssetType.Text(Static.terrainJsonRef, AssetPath(baseUrl + "assets/terrain.json"))
      )

  object Sounds:
    val shanty: AssetName       = AssetName("shanty")
    val walkSound: AssetName    = AssetName("walk")
    val respawnSound: AssetName = AssetName("respawn")
    val jumpSound: AssetName    = AssetName("jump")

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Audio(Sounds.shanty, AssetPath(baseUrl + "assets/bgmusic.mp3")),
        AssetType.Audio(Sounds.walkSound, AssetPath(baseUrl + "assets/walk.mp3")),
        AssetType.Audio(Sounds.jumpSound, AssetPath(baseUrl + "assets/jump.mp3")),
        AssetType.Audio(Sounds.respawnSound, AssetPath(baseUrl + "assets/respawn.mp3"))
      )

  object Clouds:

    val bigCloudsRef: AssetName   = AssetName("Big Clouds")
    val smallCloudsRef: AssetName = AssetName("small_clouds")

    val bigCloudsGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 448, 101), 40, Material.Bitmap(bigCloudsRef)).withRef(0, 101)
    val bigCloudsWidth: Int = bigCloudsGraphic.crop.width

    val cloud1: Graphic[Material.Bitmap] = Graphic(Rectangle(0, 0, 140, 39), 45, Material.Bitmap(smallCloudsRef))
    val cloud2: Graphic[Material.Bitmap] = Graphic(Rectangle(0, 39, 140, 39), 45, Material.Bitmap(smallCloudsRef))
    val cloud3: Graphic[Material.Bitmap] = Graphic(Rectangle(0, 78, 140, 39), 45, Material.Bitmap(smallCloudsRef))

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Clouds.bigCloudsRef, AssetPath(baseUrl + "assets/" + Clouds.bigCloudsRef + ".png")),
        AssetType.Image(Clouds.smallCloudsRef, AssetPath(baseUrl + "assets/" + Clouds.smallCloudsRef + ".png"))
      )

  object Water:
    val ref: AssetName     = AssetName("Water Reflect")
    val jsonRef: AssetName = AssetName("Water Reflect JSON")

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Water.ref, AssetPath(baseUrl + "assets/" + Water.ref + ".png")),
        AssetType.Text(Water.jsonRef, AssetPath(baseUrl + "assets/" + Water.ref + ".json"))
      )

  object Flag:
    val ref: AssetName     = AssetName("Flag")
    val jsonRef: AssetName = AssetName("Flag JSON")

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Flag.ref, AssetPath(baseUrl + "assets/" + Flag.ref + ".png")),
        AssetType.Text(Flag.jsonRef, AssetPath(baseUrl + "assets/" + Flag.ref + ".json"))
      )

  object Captain:
    val ref: AssetName     = AssetName("Captain Clown Nose")
    val jsonRef: AssetName = AssetName("Captain Clown Nose JSON")

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Captain.ref, AssetPath(baseUrl + "assets/" + Captain.ref + ".png")),
        AssetType.Text(Captain.jsonRef, AssetPath(baseUrl + "assets/" + Captain.ref + ".json"))
      )

  object Trees:
    val ref: AssetName     = AssetName("Palm Tree")
    val jsonRef: AssetName = AssetName("Palm Tree JSON")

    val trunksRef: AssetName = AssetName("Front Palm Trees")

    val tallTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Bitmap(trunksRef))
        .withCrop(Rectangle(8, 0, 16, 60))

    val leftLeaningTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Bitmap(trunksRef))
        .withCrop(Rectangle(43, 0, 50, 22))

    val rightLeaningTrunkGraphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 96, 96), 1, Material.Bitmap(trunksRef))
        .withCrop(Rectangle(36, 32, 48, 23))

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Trees.trunksRef, AssetPath(baseUrl + "assets/" + Trees.trunksRef + ".png")),
        AssetType.Image(Trees.ref, AssetPath(baseUrl + "assets/" + Trees.ref + ".png")),
        AssetType.Text(Trees.jsonRef, AssetPath(baseUrl + "assets/" + Trees.ref + ".json"))
      )

  object Helm:
    val ref: AssetName     = AssetName("Ship Helm")
    val jsonRef: AssetName = AssetName("Ship Helm JSON")

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Helm.ref, AssetPath(baseUrl + "assets/" + Helm.ref + ".png")),
        AssetType.Text(Helm.jsonRef, AssetPath(baseUrl + "assets/" + Helm.ref + ".json"))
      )

  object Fonts:
    val smallFontName: AssetName            = AssetName("smallFontName")
    val fontKey: FontKey                    = FontKey("boxy font")
    val fontMaterial: Material.ImageEffects = Material.ImageEffects(smallFontName)

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

    def assets(baseUrl: String): Set[AssetType] =
      Set(
        AssetType.Image(Fonts.smallFontName, AssetPath(baseUrl + "assets/boxy_font_small.png"))
      )

  def initialAssets(baseUrl: String): Set[AssetType] =
    Fonts.assets(baseUrl) ++
      Captain.assets(baseUrl)

  def remainingAssets(baseUrl: String): Set[AssetType] =
    Static.assets(baseUrl) ++
      Sounds.assets(baseUrl) ++
      Clouds.assets(baseUrl) ++
      Water.assets(baseUrl) ++
      Flag.assets(baseUrl) ++
      Trees.assets(baseUrl) ++
      Helm.assets(baseUrl)
