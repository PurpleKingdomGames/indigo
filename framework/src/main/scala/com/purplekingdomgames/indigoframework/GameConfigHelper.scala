package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.GameConfig
import com.purplekingdomgames.indigo.gameengine.assets.{AssetManager, TextAsset}
import com.purplekingdomgames.indigo.util.Logger
import upickle.default._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GameConfigHelper {

  def load: Future[Option[GameConfig]] =
    AssetManager
      .loadTextAsset(TextAsset("assetsList", "assets/config.json"))
      .map { p =>
        fromJson(p.contents)
      }

  def fromJson(json: String): Option[GameConfig] = {
    try {
      Option(read[GameConfig](json))
    } catch {
      case e: Throwable =>
        Logger.info("Failed to deserialise json into a useable GameConfig: " + e.getMessage)
        None
    }
  }

}
