package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.events.MouseEvent
import indigo.shared.events.PointerType
import indigo.shared.events.ScrollDirection

import scala.annotation.nowarn

@nowarn("msg=deprecated")
final class Mouse(val pointers: Pointers, val wheelEvents: Batch[MouseEvent.Wheel]) extends PointerState {
  val pointerType: Option[PointerType] = Some(PointerType.Mouse)

  @deprecated("Use `InputState.Wheel` instead", "0.22.0")
  lazy val scrolled: Option[ScrollDirection] =

    @nowarn("msg=deprecated")
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[ScrollDirection]
    else if amount < 0 then Some(ScrollDirection.ScrollUp)
    else Some(ScrollDirection.ScrollDown)
}
object Mouse:
  @nowarn("msg=deprecated")
  val default: Mouse = Mouse(Pointers.default, Batch.empty)
