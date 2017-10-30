package com.purplekingdomgames

import com.purplekingdomgames.shared.{AssetType, GameConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object indigo {

  implicit val emptyConfigAsync: Future[Option[GameConfig]] = Future(None)

  implicit val emptyAssetsAsync: Future[Set[AssetType]] = Future(Set())

}
