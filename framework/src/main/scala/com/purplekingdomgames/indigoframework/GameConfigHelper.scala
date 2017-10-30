package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.assets.AssetManager
import com.purplekingdomgames.indigo.util.Logger
import com.purplekingdomgames.shared.{GameConfig, TextAsset}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GameConfigHelper {

  def load: Future[Option[GameConfig]] =
    AssetManager
      .loadTextAsset(TextAsset("assetsList", "assets/config.json"))
      .map { p =>
        fromJson(p.contents)
      }

  def fromJson(json: String): Option[GameConfig] =
    GameConfig.fromJson(json) match {
      case Right(c) => Some(c)
      case Left(e) =>
        Logger.info(e)
        None
    }

}
