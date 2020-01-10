package indigo.shared.events

import indigo.shared.datatypes.Point
import indigo.shared.constants.Key
import indigo.shared.datatypes.Rectangle

final class InputSignals(protected val inputEvents: List[InputEvent], val mousePosition: Point, val leftMouseIsDown: Boolean) extends MouseSignals with KeyboardSignals

object InputSignals {
  val default: InputSignals =
    new InputSignals(
      inputEvents = Nil,
      mousePosition = Point.zero,
      leftMouseIsDown = false
    )
}

sealed trait MouseSignals {

  protected val inputEvents: List[InputEvent]

  private lazy val mouseEvents: List[MouseEvent] = inputEvents.collect { case e: MouseEvent => e }

  val mousePosition: Point

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

  def wasMousePositionAt(position: Point): Boolean = position === mousePosition
  def wasMousePositionAt(x: Int, y: Int): Boolean  = wasMousePositionAt(Point(x, y))

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

  def wasMousePositionWithin(bounds: Rectangle): Boolean = bounds.isPointWithin(mousePosition)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMousePositionWithin(Rectangle(x, y, width, height))

}

sealed trait KeyboardSignals {

  protected val inputEvents: List[InputEvent]

  private lazy val keyboardEvents: List[KeyboardEvent] = inputEvents.collect { case e: KeyboardEvent => e }

  lazy val keysUp: List[Key]   = keyboardEvents.collect { case k: KeyboardEvent.KeyUp   => k.keyCode }
  lazy val keysDown: List[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyDown => k.keyCode }

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysUp.contains(keyCode))

}
