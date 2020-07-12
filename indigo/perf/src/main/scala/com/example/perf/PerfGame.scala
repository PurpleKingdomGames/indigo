package com.example.perf

import indigo._
import indigo.json.Json
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object PerfGame extends IndigoDemo[Unit, Dude, DudeModel, Unit] {

  val targetFPS: Int          = 60
  val viewportWidth: Int      = 800
  val viewportHeight: Int     = 600
  val magnificationLevel: Int = 1

  val eventFilters: EventFilters =
    EventFilters(
      {
        case e: FrameTick =>
          Some(e)

        case e: KeyboardEvent.KeyDown =>
          Some(e)

        case e: KeyboardEvent.KeyUp =>
          Some(e)

        case _ =>
          None
      },
      _ => None
    )

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
      .withSubSystems(FPSCounter(PerfView.fontKey, Point(10, 565), targetFPS))

  def initialModel(startupData: Dude): DudeModel =
    PerfModel.initialModel(startupData)

  def initialViewModel(startupData: Dude, model: DudeModel): Unit =
    ()

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, Dude] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[Dude] =
      Startup
        .Success(
          Dude(
            aseprite,
            spriteAndAnimations.sprite
              .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
              .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[Dude]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(PerfAssets.dudeName.value + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, Depth(3), PerfAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def updateModel(context: FrameContext[Dude], model: DudeModel): GlobalEvent => Outcome[DudeModel] =
    PerfModel.updateModel(model)

  def updateViewModel(context: FrameContext[Dude], model: DudeModel, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[Dude], model: DudeModel, viewModel: Unit): SceneUpdateFragment =
    PerfView.updateView(model, context.inputState)

}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
