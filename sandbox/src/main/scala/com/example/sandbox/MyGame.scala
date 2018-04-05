package com.example.sandbox

import com.purplekingdomgames.indigo.IndigoGameBasic
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Depth, FontInfo}
import com.purplekingdomgames.indigoexts.formats.{Aseprite, AsepriteHelper}
import com.purplekingdomgames.shared.{AssetType, ClearColor, GameConfig, GameViewport}

object MyGame extends IndigoGameBasic[MyStartupData, MyGameModel] {

  private val viewportWidth: Int = 456
  private val viewportHeight: Int = 256
  private val magnificationLevel: Int = 2

  val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0.4, 0.2, 0.5, 1),
    magnification = magnificationLevel
  )

  val assets: Set[AssetType] = MyAssets.assets

  val fonts: Set[FontInfo] = Set(MyView.fontInfo)
  val animations: Set[Animations] = Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartupData] = {
    val dude = for {
      json <- assetCollection.texts.find(p => p.name == MyAssets.dudeName + "-json").map(_.contents)
      aseprite <- AsepriteHelper.fromJson(json)
      spriteAndAnimations <- AsepriteHelper.toSpriteAndAnimations(aseprite, Depth(3), MyAssets.dudeName)
      _ <- Option(registerAnimations(spriteAndAnimations.animations))
    } yield Dude(
      aseprite,
      spriteAndAnimations.sprite
        .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
        .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
    )

    dude match {
      case Some(d) => Right(MyStartupData(d))
      case None => Left(StartupErrors("Failed to load the dude"))
    }
  }

  def initialModel(startupData: MyStartupData): MyGameModel =
    MyModel.initialModel(startupData)

  def update(gameTime: GameTime, model: MyGameModel): GameEvent => MyGameModel =
    MyModel.updateModel(model)

  def present(gameTime: GameTime, model: MyGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    MyView.updateView(model, frameInputEvents)
}

case class Dude(aseprite: Aseprite, sprite: Sprite)
case class MyStartupData(dude: Dude)
