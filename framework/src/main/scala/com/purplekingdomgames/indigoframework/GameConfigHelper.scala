package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.assets.AssetManager
import com.purplekingdomgames.indigo.runtime.IndigoLogger
import com.purplekingdomgames.shared.{AssetType, GameConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GameConfigHelper {

  /*
  Looks useful:

val xhr = new XMLHttpRequest()

xhr.open("GET",
  "https://api.twitter.com/1.1/search/" +
  "tweets.json?q=%23scalajs"
)
xhr.onload = { (e: Event) =>
  if (xhr.status == 200) {
    val r = JSON.parse(xhr.responseText)
    $("#tweets").html(parseTweets(r))
  }
}
xhr.send()
   */

  def load: Future[Option[GameConfig]] =
    AssetManager
      .loadTextAsset(AssetType.Text("assetsList", "assets/config.json"))
      .map { p =>
        fromJson(p.contents)
      }

  def fromJson(json: String): Option[GameConfig] =
    GameConfig.fromJson(json) match {
      case Right(c) => Some(c)
      case Left(e) =>
        IndigoLogger.info(e)
        None
    }

}
