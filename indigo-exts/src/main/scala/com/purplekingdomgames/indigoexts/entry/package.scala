package com.purplekingdomgames.indigoexts

import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
import com.purplekingdomgames.shared.{AssetType, GameConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object entry {

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

  val defaultGameConfig: GameConfig = com.purplekingdomgames.shared.GameConfig.default

  val noRender: SceneUpdateFragment = com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment.empty

}
