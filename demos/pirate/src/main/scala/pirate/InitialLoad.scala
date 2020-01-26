package pirate

import indigo._
import indigo.json.Json
import indigoexts.formats._

object InitialLoad {

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData] = {

    val loadedReflections: Option[SpriteAndAnimations] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Water.jsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(20), Assets.Water.ref)
    } yield spriteAndAnimations

    val loadedFlag: Option[SpriteAndAnimations] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Flag.jsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(10), Assets.Flag.ref)
    } yield spriteAndAnimations

    val loadedCaptain: Option[SpriteAndAnimations] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Captain.jsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(2), Assets.Captain.ref)
    } yield spriteAndAnimations

    (loadedReflections, loadedFlag, loadedCaptain) match {
      case (Some(reflections), Some(flag), Some(captain)) =>
        makeStartupData(reflections, flag, captain)

      case (None, _, _) =>
        Startup.Failure(StartupErrors("Failed to load the water reflections"))

      case (_, None, _) =>
        Startup.Failure(StartupErrors("Failed to load the flag"))

      case (_, _, None) =>
        Startup.Failure(StartupErrors("Failed to load the captain"))

    }
  }

  def makeStartupData(waterReflections: SpriteAndAnimations, flag: SpriteAndAnimations, captain: SpriteAndAnimations): Startup.Success[StartupData] =
    Startup
      .Success(
        StartupData(waterReflections.sprite, flag.sprite.withDepth(Depth(10)), captain.sprite.withDepth(Depth(2)))
      )
      .addAnimations(waterReflections.animations, flag.animations, captain.animations)

}

final case class StartupData(waterReflections: Sprite, flag: Sprite, captain: Sprite)
