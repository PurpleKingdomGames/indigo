package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import indigo.shared.formats.{Aseprite}
import indigo.json._;

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Json")
@JSExportAll
object JsonDelegate {

  def asepriteFromJson(json: String): js.UndefOr[Aseprite] =
    Json.asepriteFromJson(json).orUndefined
}
