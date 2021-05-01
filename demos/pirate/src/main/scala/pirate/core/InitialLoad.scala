package pirate.core

import indigo._
import indigo.json.Json
import indigo.shared.formats.TiledGridMap

/*
In a nutshell, the setup function here takes the boot data (screen dimensions),
the asset collection, and a dice object, and produces "start up data", which is
totally user defined and you can do that however you like, you just need to return
a success or failure object.

What's really important to understand is that this function is run _more than once!_

The first time it runs, we only have available the assets we told indigo we needed
for the loading screen. We find this out by simply checking which assets are available
at the moment.

The second run is triggered by the completion of a dynamic asset load - you see the
progress of which on the loading screen. This can theoretically happen as many times
as you decide to load assets. So it's only on the second run that we do all the work
in `makeAdditionalAssets`.
 */
object InitialLoad {

  def setup(
      screenDimensions: Rectangle,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[StartupData]] =
    Outcome(
      loadAnimation(assetCollection, dice)(Assets.Captain.jsonRef, Assets.Captain.ref, Depth(2))
        .map { captain =>
          makeStartupData(
            captain,
            levelDataStore(screenDimensions, assetCollection, dice)
          )
        } match {
        case Left(message) =>
          Startup.Failure(message)

        case Right(success) =>
          success
      }
    )

  def levelDataStore(
      screenDimensions: Rectangle,
      assetCollection: AssetCollection,
      dice: Dice
  ): Option[(LevelDataStore, List[Animation])] = {

    val loader: (AssetName, AssetName, Depth) => Either[String, SpriteAndAnimations] =
      loadAnimation(assetCollection, dice)

    // If these assets haven't been loaded yet, we're not going to try and process anything.
    if (
      assetCollection.findTextDataByName(Assets.Helm.jsonRef).isDefined &&
      assetCollection.findTextDataByName(Assets.Trees.jsonRef).isDefined &&
      assetCollection.findTextDataByName(Assets.Water.jsonRef).isDefined &&
      assetCollection.findTextDataByName(Assets.Flag.jsonRef).isDefined &&
      assetCollection.findTextDataByName(Assets.Static.terrainJsonRef).isDefined
    ) {

      val tileMapper: Int => TileType = {
        case 0 => TileType.Empty
        case _ => TileType.Solid
      }

      // Here we read the Tiled level description and manufacture a triple of:
      // (the tile size, a `TiledGridMap` of data, and a renderable verison of the map)
      val terrainData: Option[(Point, TiledGridMap[TileType], Group)] =
        for {
          json         <- assetCollection.findTextDataByName(Assets.Static.terrainJsonRef)
          tileMap      <- Json.tiledMapFromJson(json)
          terrainGroup <- tileMap.toGroup(Assets.Static.terrainRef)
          grid         <- tileMap.toGrid(tileMapper)
        } yield (Point(tileMap.tilewidth, tileMap.tileheight), grid, terrainGroup.withDepth(Depth(4)))

      for {
        helm        <- loader(Assets.Helm.jsonRef, Assets.Helm.ref, Depth(9)).toOption
        palm        <- loader(Assets.Trees.jsonRef, Assets.Trees.ref, Depth(1)).toOption
        reflections <- loader(Assets.Water.jsonRef, Assets.Water.ref, Depth(20)).toOption
        flag        <- loader(Assets.Flag.jsonRef, Assets.Flag.ref, Depth(10)).toOption
        terrain     <- terrainData
      } yield makeAdditionalAssets(screenDimensions, helm, palm, reflections, flag, terrain._1, terrain._2, terrain._3)
    } else None
  }

  // Helper function that loads Aseprite animations.
  def loadAnimation(
      assetCollection: AssetCollection,
      dice: Dice
  )(jsonRef: AssetName, name: AssetName, depth: Depth): Either[String, SpriteAndAnimations] = {
    given CanEqual[Option[SpriteAndAnimations], Option[SpriteAndAnimations]] = CanEqual.derived

    val res = for {
      json                <- assetCollection.findTextDataByName(jsonRef)
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, name)
    } yield spriteAndAnimations.copy(sprite = spriteAndAnimations.sprite.withDepth(depth))

    res match {
      case Some(spriteAndAnimations) =>
        Right(spriteAndAnimations)

      case None =>
        Left("Failed to load " + name)
    }
  }

  def makeAdditionalAssets(
      screenDimensions: Rectangle,
      helm: SpriteAndAnimations,
      palm: SpriteAndAnimations,
      waterReflections: SpriteAndAnimations,
      flag: SpriteAndAnimations,
      tileSize: Point,
      terrainMap: TiledGridMap[TileType],
      terrain: Group
  ): (LevelDataStore, List[Animation]) =
    (
      LevelDataStore(
        waterReflections.sprite
          .withRef(85, 0)
          .moveTo(screenDimensions.horizontalCenter, screenDimensions.verticalCenter + 5),
        flag.sprite.withRef(22, 105).moveTo(200, 288),
        helm.sprite.withRef(31, 49).moveTo(605, 160),
        palm.sprite,
        tileSize,
        terrainMap,
        terrain
      ),
      List(waterReflections.animations, flag.animations, helm.animations, palm.animations)
    )

  def makeStartupData(
      captain: SpriteAndAnimations,
      levelDataStore: Option[(LevelDataStore, List[Animation])]
  ): Startup.Success[StartupData] =
    Startup
      .Success(
        StartupData(
          captain.sprite
            .withMaterial {
              captain.sprite.material match {
                case m: Material.ImageEffects => m
                case m: Material.Bitmap       => Material.ImageEffects(m.diffuse)
              }
            }
            .withRef(37, 64)
            .moveTo(300, 271),
          levelDataStore.map(_._1)
        )
      )
      .addAnimations(captain.animations)
      .addAnimations(levelDataStore.map(_._2).getOrElse(Nil))

}

final case class StartupData(
    captain: Sprite,
    levelDataStore: Option[LevelDataStore]
)
final case class LevelDataStore(
    waterReflections: Sprite,
    flag: Sprite,
    helm: Sprite,
    palm: Sprite,
    tileSize: Point,
    terrainMap: TiledGridMap[TileType],
    terrain: Group
) {
  val backTallPalm: Sprite =
    palm
      .withBindingKey(BindingKey("Back Tall Palm"))
      .withDepth(Depth(10))
}

enum TileType derives CanEqual:
  case Empty, Solid
