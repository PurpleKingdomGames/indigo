package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.events.MouseEvent
import indigo.shared.events.MouseWheel
import indigo.shared.events.PointerType

final class Mouse(val pointers: Pointers, val wheelEvents: Batch[MouseEvent.Wheel]) extends PointerState {
  val pointerType: Option[PointerType] = Some(PointerType.Mouse)

  lazy val scrolled: Option[MouseWheel] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[MouseWheel]
    else if amount < 0 then Some(MouseWheel.ScrollUp)
    else Some(MouseWheel.ScrollDown)
}
object Mouse:
  val default: Mouse = Mouse(Pointers.default, Batch.empty)
