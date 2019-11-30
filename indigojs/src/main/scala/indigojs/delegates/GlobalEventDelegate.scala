package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.events.GlobalEvent

@JSExportTopLevel("GlobalEvent")
final class GlobalEventDelegate(val eventType: String, val payload: js.Object) extends GlobalEvent
