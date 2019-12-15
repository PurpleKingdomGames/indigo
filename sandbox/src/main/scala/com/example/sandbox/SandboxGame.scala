package com.example.sandbox

import indigo._
import indigo.json.Json
import indigoexts.entrypoint._
import indigoexts.formats._
import indigoexts.subsystems.fpscounter.FPSCounter

object SandboxGame extends IndigoGameBasic[SandboxStartupData, SandboxGameModel, Unit] {

  val targetFPS: Int = 60

  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    SandboxAssets.assets

  val fonts: Set[FontInfo] =
    Set(SandboxView.fontInfo)

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set(FPSCounter.subSystem(SandboxView.fontKey, Point(3, 100), targetFPS))

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, SandboxStartupData] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(3), SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): SandboxGameModel =
    SandboxModel.initialModel(startupData)

  def update(gameTime: GameTime, model: SandboxGameModel, dice: Dice): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(model)

  def initialViewModel(startupData: SandboxStartupData): SandboxGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: SandboxGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: SandboxGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SandboxView.updateView(model, frameInputEvents)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class SandboxStartupData(dude: Dude)
