package indigo.shared.events

import indigo.shared.datatypes.Point
import indigo.shared.constants.KeyCode

final case class Signals(mousePosition: Point, keysDown: Set[KeyCode], lastKeyHeldDown: Option[KeyCode], leftMouseHeldDown: Boolean)
object Signals {
  val default: Signals = Signals(
    mousePosition = Point.zero,
    keysDown = Set(),
    lastKeyHeldDown = None,
    leftMouseHeldDown = false
  )
}