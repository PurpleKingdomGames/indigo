package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.events.WheelDirection
import indigo.shared.events.WheelEvent

final class Wheel(val wheelEvents: Batch[WheelEvent.Move]) {

  lazy val verticalScroll: Option[WheelDirection] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[WheelDirection]
    else if amount < 0 then Some(WheelDirection.Up)
    else Some(WheelDirection.Down)

  lazy val horizontalScroll: Option[WheelDirection] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaX
    }

    if amount == 0 then Option.empty[WheelDirection]
    else if amount < 0 then Some(WheelDirection.Left)
    else Some(WheelDirection.Right)
}

object Wheel:
  val default: Wheel = Wheel(Batch.empty)
