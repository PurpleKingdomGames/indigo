package indigoframework

import indigo.platform.assets.AssetCollection
import indigo.shared.IndigoLogger
import indigo.shared.{AssetType, GameConfig}
import indigo.json._

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
    AssetCollection
      .loadTextAsset(AssetType.Text("assetsList", "assets/config.json"))
      .map { p =>
        fromJson(p.data)
      }

  def fromJson(json: String): Option[GameConfig] =
    gameConfigFromJson(json) match {
      case Right(c) => Some(c)
      case Left(e) =>
        IndigoLogger.info(e)
        None
    }

}
