package indigo.shared.events

import indigo.shared.datatypes.Point
import indigo.shared.constants.Key

final case class InputSignals(mousePosition: Point, keysDown: Set[Key], lastKeyHeldDown: Option[Key], leftMouseHeldDown: Boolean)
object InputSignals {
  val default: InputSignals = InputSignals(
    mousePosition = Point.zero,
    keysDown = Set(),
    lastKeyHeldDown = None,
    leftMouseHeldDown = false
  )
}
