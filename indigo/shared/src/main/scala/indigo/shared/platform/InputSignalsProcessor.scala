package indigo.shared.platform

import indigo.shared.events.InputSignals
import indigo.shared.events.{GlobalEvent, MouseEvent, KeyboardEvent}

trait InputSignalsProcessor {
  def calculate(previous: InputSignals, events: List[GlobalEvent]): InputSignals
}
object InputSignalsProcessor {
  val default: InputSignalsProcessor =
    new InputSignalsProcessor {
      def calculate(previous: InputSignals, events: List[GlobalEvent]): InputSignals =
        events.foldLeft(previous) { (signals, e) =>
          e match {
            case mp: MouseEvent.Move =>
              signals.copy(mousePosition = mp.position)

            case _: MouseEvent.MouseDown =>
              signals.copy(leftMouseHeldDown = true)

            case _: MouseEvent.MouseUp =>
              signals.copy(leftMouseHeldDown = false)

            case e: KeyboardEvent.KeyDown =>
              signals.copy(
                keysDown = signals.keysDown + e.keyCode,
                lastKeyHeldDown = Some(e.keyCode)
              )

            case e: KeyboardEvent.KeyUp =>
              val keysDown = signals.keysDown.filterNot(_ === e.keyCode)

              val lastKey = signals.lastKeyHeldDown.flatMap { key =>
                if (key === e.keyCode || !keysDown.contains(key)) None
                else Some(key)
              }

              signals.copy(
                keysDown = keysDown,
                lastKeyHeldDown = lastKey
              )

            case _ =>
              signals
          }
        }
    }
}
