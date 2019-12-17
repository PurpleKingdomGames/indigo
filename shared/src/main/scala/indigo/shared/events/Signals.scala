package indigo.shared.events

import indigo.shared.datatypes.Point
import indigo.shared.constants.Key

final case class Signals(mousePosition: Point, keysDown: Set[Key], lastKeyHeldDown: Option[Key], leftMouseHeldDown: Boolean)
object Signals {
  val default: Signals = Signals(
    mousePosition = Point.zero,
    keysDown = Set(),
    lastKeyHeldDown = None,
    leftMouseHeldDown = false
  )
}
