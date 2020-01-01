package indigojs.delegates

import indigo.shared.events.Signals

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class SignalsDelegate(signals: Signals) {

  @JSExport
  val mousePosition: PointDelegate =
    PointDelegate.fromPoint(signals.mousePosition)

  @JSExport
  val keysDown: Set[KeyDelegate] =
    signals.keysDown.map(KeyDelegate.fromKey)

  @JSExport
  val lastKeyHeldDown: Option[KeyDelegate] =
    signals.lastKeyHeldDown.map(KeyDelegate.fromKey)

  @JSExport
  val leftMouseHeldDown: Boolean =
    signals.leftMouseHeldDown
}
