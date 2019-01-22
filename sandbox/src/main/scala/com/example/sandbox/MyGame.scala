package com.example.sandbox

import indigo._
import indigoexts.entrypoint._
import indigoexts.formats._

import indigo.Eq._

object MyGame extends IndigoGameBasic[MyStartupData, MyGameModel, MyViewModel] {

  private val viewportWidth: Int      = 456
  private val viewportHeight: Int     = 256
  private val magnificationLevel: Int = 2

  val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0.4, 0.2, 0.5, 1),
    magnification = magnificationLevel
  )

  val assets: Set[AssetType] = MyAssets.assets

  val fonts: Set[FontInfo]        = Set(MyView.fontInfo)
  val animations: Set[Animations] = Set()
  val subSystems: Set[SubSystem]  = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, MyStartupData] = {
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[MyStartupData] =
      Startup
        .Success(
          MyStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[MyStartupData]] = for {
      json                <- assetCollection.texts.find(p => p.name === MyAssets.dudeName + "-json").map(_.contents)
      aseprite            <- Aseprite.fromJson(json)
      spriteAndAnimations <- Aseprite.toSpriteAndAnimations(aseprite, Depth(3), MyAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(StartupErrors("Failed to load the dude")))
  }

  def initialModel(startupData: MyStartupData): MyGameModel =
    MyModel.initialModel(startupData)

  def update(gameTime: GameTime, model: MyGameModel): GlobalEvent => UpdatedModel[MyGameModel] =
    MyModel.updateModel(model)

  def initialViewModel(startupData: MyStartupData): MyGameModel => MyViewModel = _ => MyViewModel()

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[MyViewModel] =
    UpdatedViewModel(viewModel)

  def present(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    MyView.updateView(model, frameInputEvents)
}

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class MyStartupData(dude: Dude)
final case class MyViewModel()
