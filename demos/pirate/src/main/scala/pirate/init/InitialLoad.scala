package pirate.init

import indigo._
import indigo.json.Json
import indigoexts.formats._

object InitialLoad {

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData] = {

    val loader: (AssetName, AssetName, Depth) => Either[String, SpriteAndAnimations] =
      loadAnimation(assetCollection, dice)

    val res = for {
      reflections <- loader(Assets.Water.jsonRef, Assets.Water.ref, Depth(20))
      flag        <- loader(Assets.Flag.jsonRef, Assets.Flag.ref, Depth(10))
      captain     <- loader(Assets.Captain.jsonRef, Assets.Captain.ref, Depth(2))
      helm        <- loader(Assets.Helm.jsonRef, Assets.Helm.ref, Depth(9))
      palm        <- loader(Assets.Trees.jsonRef, Assets.Trees.ref, Depth(1))
    } yield makeStartupData(reflections, flag, captain, helm, palm)

    res match {
      case Left(message) =>
        Startup.Failure(StartupErrors(message))

      case Right(success) =>
        success
    }
  }

  def loadAnimation(assetCollection: AssetCollection, dice: Dice)(jsonRef: AssetName, name: AssetName, depth: Depth): Either[String, SpriteAndAnimations] = {
    val res = for {
      json                <- assetCollection.findTextDataByName(jsonRef)
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(dice, aseprite, depth, name)
    } yield spriteAndAnimations
   
    res match {
      case Some(spriteAndAnimations) =>
        Right(spriteAndAnimations)

      case None =>
        Left("Failed to load " + name)
    }
  }

  def makeStartupData(
      waterReflections: SpriteAndAnimations,
      flag: SpriteAndAnimations,
      captain: SpriteAndAnimations,
      helm: SpriteAndAnimations,
      palm: SpriteAndAnimations
  ): Startup.Success[StartupData] =
    Startup
      .Success(
        StartupData(waterReflections.sprite, flag.sprite, captain.sprite, helm.sprite, palm.sprite)
      )
      .addAnimations(
        waterReflections.animations,
        flag.animations,
        captain.animations,
        helm.animations,
        palm.animations,
        palm.animations.withAnimationKey(AnimationKey("P Back Tall"))
      )

}

final case class StartupData(waterReflections: Sprite, flag: Sprite, captain: Sprite, helm: Sprite, palm: Sprite)
