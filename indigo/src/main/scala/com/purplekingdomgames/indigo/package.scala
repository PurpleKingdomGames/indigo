package com.purplekingdomgames

import com.purplekingdomgames.indigo.gameengine.GameConfig
import com.purplekingdomgames.indigo.gameengine.assets.AssetType

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object indigo {

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

}
