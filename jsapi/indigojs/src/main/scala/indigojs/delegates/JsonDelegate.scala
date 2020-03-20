package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import indigo.json._;
import indigojs.delegates.formats._;
import indigojs.delegates.formats.AsepriteUtilities._;

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
@JSExportTopLevel("Json")
@JSExportAll
object JsonDelegate {

  def asepriteFromJson(json: String): js.UndefOr[AsepriteDelegate] =
    Json.asepriteFromJson(json) match {
        case Some(a) => Some(a.toJsDelegate).orUndefined
        case None => None.orUndefined
    }
}
