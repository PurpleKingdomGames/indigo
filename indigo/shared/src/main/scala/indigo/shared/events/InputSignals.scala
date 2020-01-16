package indigo.shared.events

import indigo.shared.datatypes.Point
import indigo.shared.constants.Key
import indigo.shared.datatypes.Rectangle
import scala.annotation.tailrec

final class InputSignals(val mouse: MouseSignals, val keyboard: KeyboardSignals) {
  def calculateNext(events: List[InputEvent]): InputSignals =
    InputSignals.calculateNext(this, events)
}

object InputSignals {
  val default: InputSignals =
    new InputSignals(MouseSignals.default, KeyboardSignals.default)

  def calculateNext(previous: InputSignals, events: List[InputEvent]): InputSignals =
    new InputSignals(
      MouseSignals.calculateNext(previous.mouse, events.collect { case e: MouseEvent => e }),
      KeyboardSignals.calculateNext(previous.keyboard, events.collect { case e: KeyboardEvent => e })
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
}

final class MouseSignals(mouseEvents: List[MouseEvent], val position: Point, val leftMouseIsDown: Boolean) {

  lazy val mousePressed: Boolean =
    mouseEvents.exists {
      case _: MouseEvent.MouseDown => true
      case _                       => false
    }
  lazy val mouseReleased: Boolean =
    mouseEvents.exists {
      case _: MouseEvent.MouseUp => true
      case _                     => false
    }
  lazy val mouseClicked: Boolean =
    mouseEvents.exists {
      case _: MouseEvent.Click => true
      case _                   => false
    }

  lazy val mouseClickAt: Option[Point] = mouseEvents.collectFirst { case m: MouseEvent.Click     => m.position }
  lazy val mouseUpAt: Option[Point]    = mouseEvents.collectFirst { case m: MouseEvent.MouseUp   => m.position }
  lazy val mouseDownAt: Option[Point]  = mouseEvents.collectFirst { case m: MouseEvent.MouseDown => m.position }

  private def wasMouseAt(position: Point, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => position === pt
      case None     => false
    }

  def wasMouseClickedAt(position: Point): Boolean = wasMouseAt(position, mouseClickAt)
  def wasMouseClickedAt(x: Int, y: Int): Boolean  = wasMouseClickedAt(Point(x, y))

  def wasMouseUpAt(position: Point): Boolean = wasMouseAt(position, mouseUpAt)
  def wasMouseUpAt(x: Int, y: Int): Boolean  = wasMouseUpAt(Point(x, y))

  def wasMouseDownAt(position: Point): Boolean = wasMouseAt(position, mouseDownAt)
  def wasMouseDownAt(x: Int, y: Int): Boolean  = wasMouseDownAt(Point(x, y))

  def wasMousePositionAt(target: Point): Boolean  = target === position
  def wasMousePositionAt(x: Int, y: Int): Boolean = wasMousePositionAt(Point(x, y))

  //Within
  private def wasMouseWithin(bounds: Rectangle, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => bounds.isPointWithin(pt)
      case None     => false
    }

  def wasMouseClickedWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseClickAt)
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMouseClickedWithin(Rectangle(x, y, width, height))

  def wasMouseUpWithin(bounds: Rectangle): Boolean                       = wasMouseWithin(bounds, mouseUpAt)
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseUpWithin(Rectangle(x, y, width, height))

  def wasMouseDownWithin(bounds: Rectangle): Boolean                       = wasMouseWithin(bounds, mouseDownAt)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseDownWithin(Rectangle(x, y, width, height))

  def wasMousePositionWithin(bounds: Rectangle): Boolean = bounds.isPointWithin(position)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMousePositionWithin(Rectangle(x, y, width, height))

}
object MouseSignals {
  val default: MouseSignals =
    new MouseSignals(Nil, Point.zero, false)

  def calculateNext(previous: MouseSignals, events: List[MouseEvent]): MouseSignals =
    new MouseSignals(
      events,
      lastMousePosition(previous.position, events),
      isLeftMouseDown(previous.leftMouseIsDown, events)
      )

  private def lastMousePosition(previous: Point, events: List[MouseEvent]): Point =
    events.collect { case mp: MouseEvent.Move => mp.position }.reverse.headOption match {
      case None           => previous
      case Some(position) => position
    }

  @tailrec
  private def isLeftMouseDown(isDown: Boolean, events: List[MouseEvent]): Boolean =
    events match {
      case Nil =>
        isDown

      case MouseEvent.MouseDown(_, _) :: xs =>
        isLeftMouseDown(true, xs)

      case MouseEvent.MouseUp(_, _) :: xs =>
        isLeftMouseDown(false, xs)

      case _ :: xs =>
        isLeftMouseDown(isDown, xs)
    }
}

final class KeyboardSignals(keyboardEvents: List[KeyboardEvent]) {

  lazy val keysUp: List[Key]   = keyboardEvents.collect { case k: KeyboardEvent.KeyUp   => k.keyCode }
  lazy val keysDown: List[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyDown => k.keyCode }

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysUp.contains(keyCode))

}
object KeyboardSignals {
  val default: KeyboardSignals =
    new KeyboardSignals(Nil)

  def calculateNext(previous: KeyboardSignals, events: List[KeyboardEvent]): KeyboardSignals =
    ???
}
