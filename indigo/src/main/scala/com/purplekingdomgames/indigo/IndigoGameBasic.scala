package com.purplekingdomgames.indigo

import com.purplekingdomgames.indigo.IndigoGameBase.IndigoGame
import com.purplekingdomgames.indigo.gameengine.{GameTime, Startup, StartupErrors}
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, SceneUpdateFragment}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.FontInfo
import com.purplekingdomgames.shared.{AssetType, GameConfig}

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your games model
  */
trait IndigoGameBasic[StartupData, Model] {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animations]

  def setup(assetCollection: AssetCollection): Either[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(gameTime: GameTime, model: Model): GameEvent => Model

  def present(gameTime: GameTime, model: Model, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  private def indigoGame: IndigoGame[StartupData, StartupErrors, Model] =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .withFonts(fonts)
      .withAnimations(animations)
      .startUpGameWith(ac => Startup.fromEither(setup(ac)))
      .usingInitialModel(initialModel)
      .updateModelUsing(update)
      .presentUsing(
        (gameTime: GameTime, model: Model, frameInputEvents: FrameInputEvents) => present(gameTime, model, frameInputEvents)
      )

  def registerAnimations(animations: Animations): Unit =
    indigoGame.registerAnimations(animations)

  def registerFont(fontInfo: FontInfo): Unit =
    indigoGame.registerFont(fontInfo)

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
