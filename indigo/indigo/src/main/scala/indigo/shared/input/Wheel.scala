package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.events.ScrollDirection
import indigo.shared.events.WheelEvent

final class Wheel(val wheelEvents: Batch[WheelEvent.Move]) {

  lazy val verticalScroll: Option[ScrollDirection] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[ScrollDirection]
    else if amount < 0 then Some(ScrollDirection.ScrollUp)
    else Some(ScrollDirection.ScrollDown)

  lazy val horizontalScroll: Option[ScrollDirection] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaX
    }

    if amount == 0 then Option.empty[ScrollDirection]
    else if amount < 0 then Some(ScrollDirection.ScrollLeft)
    else Some(ScrollDirection.ScrollRight)
}

object Wheel:
  val default: Wheel = Wheel(Batch.empty)
