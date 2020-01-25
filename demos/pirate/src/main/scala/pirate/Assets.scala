package pirate

import indigo._

object Assets {

  val backgroundRef: String      = "background"
  val backgroundGraphic: Graphic = Graphic(Rectangle(0, 0, 640, 360), 1, backgroundRef)

  object Clouds {

    val bigCloudsRef: String   = "Big Clouds"
    val smallCloud1Ref: String = "Small Cloud 1"
    val smallCloud2Ref: String = "Small Cloud 2"
    val smallCloud3Ref: String = "Small Cloud 3"

    val bigCloudsGraphic: Graphic = Graphic(Rectangle(0, 0, 448, 101), 1, bigCloudsRef).withRef(0, 101)
    val bigCloudsWidth: Int       = bigCloudsGraphic.bounds.width

    val cloudGraphic1: Graphic = Graphic(Rectangle(0, 0, 74, 24), 1, smallCloud1Ref)
    val cloudGraphic2: Graphic = Graphic(Rectangle(0, 0, 133, 35), 1, smallCloud2Ref)
    val cloudGraphic3: Graphic = Graphic(Rectangle(0, 0, 140, 39), 1, smallCloud3Ref)

  }

  object Water {

    val reflectionRef: String     = "Water Reflect"
    val reflectionJsonRef: String = "Water Reflect JSON"

  }

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(backgroundRef, "assets/bg.png"),
      AssetType.Image(Clouds.bigCloudsRef, "assets/" + Clouds.bigCloudsRef + ".png"),
      AssetType.Image(Clouds.smallCloud1Ref, "assets/" + Clouds.smallCloud1Ref + ".png"),
      AssetType.Image(Clouds.smallCloud2Ref, "assets/" + Clouds.smallCloud2Ref + ".png"),
      AssetType.Image(Clouds.smallCloud3Ref, "assets/" + Clouds.smallCloud3Ref + ".png"),
      AssetType.Image(Water.reflectionRef, "assets/" + Water.reflectionRef + ".png"),
      AssetType.Text(Water.reflectionJsonRef, "assets/" + Water.reflectionRef + ".json")
    )
}
