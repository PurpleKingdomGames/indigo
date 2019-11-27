package indigoframework

import indigo.platform.assets.AssetLoader
import indigo.shared.IndigoLogger
import indigo.shared.AssetType
import indigo.shared.config.GameConfig
import indigo.json.Json

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
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
    AssetLoader
      .loadTextAsset(AssetType.Text("assetsList", "assets/config.json"))
      .map { p =>
        fromJson(p.data)
      }

  def fromJson(json: String): Option[GameConfig] =
    Json.gameConfigFromJson(json) match {
      case Right(c) => Some(c)
      case Left(e) =>
        IndigoLogger.info(e)
        None
    }

}
