package com.purplekingdomgames.ninjaassault

import com.purplekingdomgames.indigo.IndigoGameBasic
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.FontInfo
import com.purplekingdomgames.ninjaassault.model.NinjaAssaultGameModel
import com.purplekingdomgames.ninjaassault.settings.{Assets, Settings}
import com.purplekingdomgames.ninjaassault.view.View
import com.purplekingdomgames.shared.{AssetType, GameConfig}

object NinjaAssult extends IndigoGameBasic[MyStartupData, NinjaAssaultGameModel] {

  val config: GameConfig =
    Settings.gameSetup

  val assets: Set[AssetType] =
    Assets.images

  val fonts: Set[FontInfo] =
    Set(Assets.fontInfo)

  val animations: Set[Animations] =
    Set()

  def setup(assetCollection: AssetCollection): Either[StartupErrors, MyStartupData] =
    Right(MyStartupData())

  def initialModel(startupData: MyStartupData): NinjaAssaultGameModel =
    NinjaAssaultGameModel.initialModel

  def update(gameTime: GameTime, model: NinjaAssaultGameModel): events.GameEvent => NinjaAssaultGameModel =
    e => NinjaAssaultGameModel.update(model)(e)

  def present(gameTime: GameTime, model: NinjaAssaultGameModel, frameInputEvents: events.FrameInputEvents): SceneUpdateFragment =
    View.draw(model, frameInputEvents)
}

case class MyStartupData()

sealed trait ViewUpdateEvent extends ViewEvent
case object JumpToMenu       extends ViewUpdateEvent
case object JumpToGame       extends ViewUpdateEvent
