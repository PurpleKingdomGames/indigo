package com.example.perf

import indigo._
import indigoextras.formats._
import indigo.json.Json
import indigoextras.subsystems.fpscounter.FPSCounter

import scala.scalajs.js.annotation._
import indigogame.IndigoDemo

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
        disableSkipModelUpdates = true,
        disableSkipViewUpdates = true
      )
    )

  val fonts: Set[FontInfo] =
    Set(PerfView.fontInfo)

  val subSystems: Set[SubSystem] =
    Set(FPSCounter.subSystem(PerfView.fontKey, Point(10, 565), targetFPS))

  def initialModel(startupData: MyStartupData): MyGameModel =
    PerfModel.initialModel(startupData)

  def initialViewModel(startupData: MyStartupData, model: MyGameModel): Unit =
    ()

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

  def updateModel(context: FrameContext, model: MyGameModel): GlobalEvent => Outcome[MyGameModel] =
    PerfModel.updateModel(model)

  def updateViewModel(context: FrameContext, model: MyGameModel, viewModel: Unit): Outcome[Unit] =
    Outcome(())

  def present(context: FrameContext, model: MyGameModel, viewModel: Unit): SceneUpdateFragment =
    PerfView.updateView(model, context.inputState)

}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class MyStartupData(dude: Dude)
