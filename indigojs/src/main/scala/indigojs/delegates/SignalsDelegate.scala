package indigojs.delegates

import indigo.shared.events.Signals

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class SignalsDelegate(signals: Signals) {

  @JSExport
  val mousePosition: PointDelegate =
    PointDelegate.fromPoint(signals.mousePosition)

  @JSExport
  val keysDown: Set[KeyCodeDelegate] =
    signals.keysDown.map(KeyCodeDelegate.fromKeyCode)

  @JSExport
  val lastKeyHeldDown: Option[KeyCodeDelegate] =
    signals.lastKeyHeldDown.map(KeyCodeDelegate.fromKeyCode)

  @JSExport
  val leftMouseHeldDown: Boolean =
    signals.leftMouseHeldDown
}
