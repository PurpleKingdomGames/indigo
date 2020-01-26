package pirate

import indigo._

object Assets {

  val shanty: String = "shanty"

  val backgroundRef: String      = "background"
  val backgroundGraphic: Graphic = Graphic(Rectangle(0, 0, 640, 360), 50, backgroundRef)

  val levelRef: String      = "level"
  val levelGraphic: Graphic = Graphic(Rectangle(0, 0, 646, 374), 1, levelRef)

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

  }

  object Water {
    val reflectionRef: String     = "Water Reflect"
    val reflectionJsonRef: String = "Water Reflect JSON"
  }

  object Flag {
    val ref: String     = "Flag"
    val jsonRef: String = "Flag JSON"
  }

  val assets: Set[AssetType] =
    Set(
      AssetType.Audio(shanty, "assets/bgmusic.mp3"),
      AssetType.Image(backgroundRef, "assets/bg.png"),
      AssetType.Image(levelRef, "assets/level.png"),
      AssetType.Image(Clouds.bigCloudsRef, "assets/" + Clouds.bigCloudsRef + ".png"),
      AssetType.Image(Clouds.smallCloud1Ref, "assets/" + Clouds.smallCloud1Ref + ".png"),
      AssetType.Image(Clouds.smallCloud2Ref, "assets/" + Clouds.smallCloud2Ref + ".png"),
      AssetType.Image(Clouds.smallCloud3Ref, "assets/" + Clouds.smallCloud3Ref + ".png"),
      AssetType.Image(Water.reflectionRef, "assets/" + Water.reflectionRef + ".png"),
      AssetType.Text(Water.reflectionJsonRef, "assets/" + Water.reflectionRef + ".json"),
      AssetType.Image(Flag.ref, "assets/" + Flag.ref + ".png"),
      AssetType.Text(Flag.jsonRef, "assets/" + Flag.ref + ".json")
    )
}
