package pirate

import indigo._
import indigo.json.Json
import indigoexts.formats._

object InitialLoad {

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData] = {
    def makeStartupData(spriteAndAnimations: SpriteAndAnimations): Startup.Success[StartupData] =
      Startup
        .Success(
          StartupData(spriteAndAnimations.sprite)
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[StartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Water.reflectionJsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(1), Assets.Water.reflectionRef)
    } yield makeStartupData(spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the water reflections")))
  }

}

final case class StartupData(waterReflections: Sprite)
