package indigo.shared.platform

import indigo.shared.events.InputSignals
import indigo.shared.events.{InputEvent, MouseEvent}
import indigo.shared.datatypes.Point
import scala.annotation.tailrec

trait InputSignalsProcessor {
  def calculate(previous: InputSignals, events: List[InputEvent]): InputSignals
}
object InputSignalsProcessor {

  def lastMousePosition(previous: Point, events: List[InputEvent]): Point =
    events.collect { case mp: MouseEvent.Move => mp.position }.reverse.headOption match {
      case None           => previous
      case Some(position) => position
    }

  @tailrec
  def isLeftMouseDown(events: List[InputEvent], isDown: Boolean): Boolean =
    events match {
      case Nil =>
        isDown

      case MouseEvent.MouseDown(_, _) :: xs =>
        isLeftMouseDown(xs, true)

      case MouseEvent.MouseUp(_, _) :: xs =>
        isLeftMouseDown(xs, false)

      case _ :: xs =>
        isLeftMouseDown(xs, isDown)
    }

  def calculateNext(previous: InputSignals, events: List[InputEvent]): InputSignals =
    new InputSignals(
      inputEvents = events,
      mousePosition = lastMousePosition(previous.mousePosition, events),
      leftMouseIsDown = isLeftMouseDown(events, previous.leftMouseIsDown)
    )

// events.foldLeft(previous) { (signals, e) =>
//   e match {
//     case mp: MouseEvent.Move =>
//       signals.copy(mousePosition = mp.position)

//     case _: MouseEvent.MouseDown =>
//       signals.copy(leftMouseHeldDown = true)

//     case _: MouseEvent.MouseUp =>
//       signals.copy(leftMouseHeldDown = false)

//     case e: KeyboardEvent.KeyDown =>
//       signals.copy(
//         keysDown = signals.keysDown + e.keyCode,
//         lastKeyHeldDown = Some(e.keyCode)
//       )

//     case e: KeyboardEvent.KeyUp =>
//       val keysDown = signals.keysDown.filterNot(_ === e.keyCode)

//       val lastKey = signals.lastKeyHeldDown.flatMap { key =>
//         if (key === e.keyCode || !keysDown.contains(key)) None
//         else Some(key)
//       }

//       signals.copy(
//         keysDown = keysDown,
//         lastKeyHeldDown = lastKey
//       )

//     case _ =>
//       signals
//   }
// }

  val default: InputSignalsProcessor =
    new InputSignalsProcessor {
      def calculate(previous: InputSignals, events: List[InputEvent]): InputSignals =
        calculateNext(previous, events)
    }
}
