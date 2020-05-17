package com.example.perf

import indigo._
import indigoexts.formats._
import indigo.json.Json
import indigoexts.subsystems.fpscounter.FPSCounter
import indigogame._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object PerfGame extends IndigoDemo[MyStartupData, MyGameModel, Unit] {

  val targetFPS: Int = 60

  val viewportWidth: Int      = 800
  val viewportHeight: Int     = 600
  val magnificationLevel: Int = 1

  val animations: Set[Animation] =
    Set()

  val assets: Set[AssetType] =
    PerfAssets.assets

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel,
      advanced = AdvancedGameConfig(
        antiAliasing = false,
        batchSize = 512,
        recordMetrics = false,
        logMetricsReportIntervalMs = 5000,
        disableSkipModelUpdates = true,
        disableSkipViewUpdates = true
      )
    )

  val fonts: Set[FontInfo] =
    Set(PerfView.fontInfo)

  val subSystems: Set[indigoexts.subsystems.SubSystem] =
    Set(FPSCounter.subSystem(PerfView.fontKey, Point(10, 565), targetFPS))

  def initialModel(startupData: MyStartupData): MyGameModel =
    PerfModel.initialModel(startupData)

  def initialViewModel(startupData: MyStartupData): MyGameModel => Unit =
    _ => ()

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, MyStartupData] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[MyStartupData] =
      Startup
        .Success(
          MyStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[MyStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(PerfAssets.dudeName.value + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(dice, aseprite, Depth(3), PerfAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] =
    PerfModel.updateModel(model)

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState, dice: Dice, boundaryLocator: BoundaryLocator): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    PerfView.updateView(model, inputState)

}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class MyStartupData(dude: Dude)
