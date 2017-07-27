package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.GameConfig
import com.purplekingdomgames.indigo.gameengine.assets.{AssetManager, TextAsset}
import com.purplekingdomgames.indigo.util.Logger
import io.circe.generic.auto._
import io.circe.parser._

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
    decode[GameConfig](json) match {
      case Right(c) => Some(c)
      case Left(e) =>
        Logger.info("Failed to deserialise json into GameConfig: " + e.getMessage)
        None
    }

}
