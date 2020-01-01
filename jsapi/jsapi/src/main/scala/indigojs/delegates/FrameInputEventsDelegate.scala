package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

import indigo.shared.events.FrameInputEvents

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class FrameInputEventsDelegate(frameInputEvents: FrameInputEvents) {

  @JSExport
  val globalEvents: js.Array[js.Object] =
    frameInputEvents.globalEvents.toJSArray.map(_.asInstanceOf[js.Object])

  @JSExport
  val signals: SignalsDelegate =
    new SignalsDelegate(frameInputEvents.signals)

}
