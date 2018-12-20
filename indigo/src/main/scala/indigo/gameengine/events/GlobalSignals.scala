package indigo.gameengine.events

import indigo.gameengine.constants.KeyCode
import indigo.gameengine.scenegraph.datatypes.Point

trait GlobalSignals {
  def calculate(previous: Signals, events: List[GlobalEvent]): Signals
}
object GlobalSignals {
  val default: GlobalSignals =
    new GlobalSignals {
      def calculate(previous: Signals, events: List[GlobalEvent]): Signals =
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

final case class Signals(mousePosition: Point, keysDown: Set[KeyCode], lastKeyHeldDown: Option[KeyCode], leftMouseHeldDown: Boolean)
object Signals {
  val default: Signals = Signals(
    mousePosition = Point.zero,
    keysDown = Set(),
    lastKeyHeldDown = None,
    leftMouseHeldDown = false
  )
}
