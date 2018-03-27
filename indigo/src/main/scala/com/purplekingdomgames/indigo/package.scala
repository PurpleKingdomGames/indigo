package com.purplekingdomgames

import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
import com.purplekingdomgames.shared.GameConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object indigo {

  type AssetCollection = com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
  type StartupErrors = com.purplekingdomgames.indigo.gameengine.StartupErrors
  type GameTime = com.purplekingdomgames.indigo.gameengine.GameTime
  type GameEvent = com.purplekingdomgames.indigo.gameengine.events.GameEvent
  type FrameInputEvents = com.purplekingdomgames.indigo.gameengine.events.FrameInputEvents
  type SceneUpdateFragment = com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
  type AssetType = com.purplekingdomgames.shared.AssetType
  type GameConfig = com.purplekingdomgames.shared.GameConfig

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

  val defaultGameConfig: GameConfig = GameConfig.default

  val noRender: SceneUpdateFragment = SceneUpdateFragment.empty

}
