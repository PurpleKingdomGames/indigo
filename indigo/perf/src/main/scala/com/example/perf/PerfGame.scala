package com.example.perf

import indigo._
import indigoextras.formats._
import indigo.json.Json
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object PerfGame extends IndigoDemo[Unit, MyStartupData, MyGameModel, Unit] {

  val targetFPS: Int          = 60
  val viewportWidth: Int      = 800
  val viewportHeight: Int     = 600
  val magnificationLevel: Int = 1

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(
        GameConfig(
          viewport = GameViewport(viewportWidth, viewportHeight),
          frameRate = targetFPS,
          clearColor = ClearColor(0.4, 0.2, 0.5, 1),
          magnification = magnificationLevel,
          advanced = AdvancedGameConfig(
            renderingTechnology = RenderingTechnology.WebGL2,
            antiAliasing = false,
            batchSize = 512,
            disableSkipModelUpdates = true,
            disableSkipViewUpdates = true
          )
        )
      )
      .withAssets(PerfAssets.assets)
      .withFonts(PerfView.fontInfo)
      .withSubSystems(FPSCounter.subSystem(PerfView.fontKey, Point(10, 565), targetFPS))

  def initialModel(startupData: MyStartupData): MyGameModel =
    PerfModel.initialModel(startupData)

  def initialViewModel(startupData: MyStartupData, model: MyGameModel): Unit =
    ()

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, MyStartupData] = {
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
