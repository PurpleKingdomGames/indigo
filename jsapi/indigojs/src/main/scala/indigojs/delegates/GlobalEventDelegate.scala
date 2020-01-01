package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.events.GlobalEvent

@JSExportTopLevel("GlobalEvent")
final class GlobalEventDelegate(val payload: js.Object) extends GlobalEvent

@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
object GlobalEventDelegate {

  def fromGlobalEvent(e: GlobalEvent): GlobalEventDelegate =
    new GlobalEventDelegate(e.asInstanceOf[js.Object])

}
