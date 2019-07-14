package com.example.sandbox

import indigo._
import indigo.json._
import indigoexts.entrypoint._
import indigoexts.formats._
import indigoexts.subsystems.fpscounter.FPSCounter

object MyGame extends IndigoGameBasic[MyStartupData, MyGameModel, Unit] {

  private val viewportWidth: Int      = 456
  private val viewportHeight: Int     = 256
  private val magnificationLevel: Int = 2

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = 60,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    MyAssets.assets

  val fonts: Set[FontInfo] =
    Set(MyView.fontInfo)

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set(FPSCounter.subSystem(MyView.fontKey, Point(3, 100)))

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, MyStartupData] = {
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
      json                <- assetCollection.findTextDataByName(AssetName(MyAssets.dudeName + "-json"))
      aseprite            <- Circe9.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(3), MyAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def initialModel(startupData: MyStartupData): MyGameModel =
    MyModel.initialModel(startupData)

  def update(gameTime: GameTime, model: MyGameModel, dice: Dice): GlobalEvent => Outcome[MyGameModel] =
    MyModel.updateModel(model)

  def initialViewModel(startupData: MyStartupData): MyGameModel => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: MyGameModel, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    MyView.updateView(model, frameInputEvents)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class MyStartupData(dude: Dude)
