package indigo.shared.input

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent

import scala.annotation.tailrec

final class Mouse(mouseEvents: List[MouseEvent], val position: Point, val leftMouseIsDown: Boolean) {

  def mouseButtonPressed(button: MouseButton): Boolean =
    mouseEvents.exists {
      case MouseEvent.MouseDown(_, mouseButton) => true
      case _                                    => false
    }

  def mouseButtonReleased(button: MouseButton): Boolean =
    mouseEvents.exists {
      case MouseEvent.MouseUp(_, button) => true
      case _                             => false
    }

  def mouseButtonClicked(button: MouseButton): Boolean =
    mouseEvents.exists {
      case MouseEvent.Click(_, button) => true
      case _                           => false
    }

  def mouseButtonClickedAt(mouseButton: MouseButton): Option[Point] = mouseEvents.collectFirst {
    case m: MouseEvent.Click if m.button == mouseButton => m.position
  }
  def mouseButtonUpAt(mouseButton: MouseButton): Option[Point] = mouseEvents.collectFirst {
    case m: MouseEvent.MouseUp if m.button == mouseButton => m.position
  }
  def mouseButtonDownAt(mouseButton: MouseButton): Option[Point] = mouseEvents.collectFirst {
    case m: MouseEvent.MouseDown if m.button == mouseButton => m.position
  }

  lazy val mouseClickAt: Option[Point] = mouseButtonClickedAt(MouseButton.LeftMouseButton)
  lazy val mouseUpAt: Option[Point]    = mouseButtonUpAt(MouseButton.LeftMouseButton)
  lazy val mouseDownAt: Option[Point]  = mouseButtonDownAt(MouseButton.LeftMouseButton)

  private def wasMouseAt(position: Point, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => position == pt
      case None     => false
    }

  def wasMouseClickedAt(position: Point): Boolean = wasMouseAt(position, mouseClickAt)
  def wasMouseClickedAt(x: Int, y: Int): Boolean  = wasMouseClickedAt(Point(x, y))

  def wasMouseUpAt(position: Point): Boolean = wasMouseAt(position, mouseUpAt)
  def wasMouseUpAt(x: Int, y: Int): Boolean  = wasMouseUpAt(Point(x, y))

  def wasMouseDownAt(position: Point): Boolean = wasMouseAt(position, mouseDownAt)
  def wasMouseDownAt(x: Int, y: Int): Boolean  = wasMouseDownAt(Point(x, y))

  def wasMousePositionAt(target: Point): Boolean  = target == position
  def wasMousePositionAt(x: Int, y: Int): Boolean = wasMousePositionAt(Point(x, y))

  // Within
  private def wasMouseWithin(bounds: Rectangle, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => bounds.isPointWithin(pt)
      case None     => false
    }

  def wasMouseClickedWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseClickAt)
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMouseClickedWithin(Rectangle(x, y, width, height))

  def wasMouseUpWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseUpAt)
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseUpWithin(
    Rectangle(x, y, width, height)
  )

  def wasMouseDownWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseDownAt)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseDownWithin(
    Rectangle(x, y, width, height)
  )

  def wasMousePositionWithin(bounds: Rectangle): Boolean = bounds.isPointWithin(position)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMousePositionWithin(Rectangle(x, y, width, height))

}
object Mouse {
  val default: Mouse =
    new Mouse(Nil, Point.zero, false)

  def calculateNext(previous: Mouse, events: List[MouseEvent]): Mouse =
    new Mouse(
      events,
      lastMousePosition(previous.position, events),
      isLeftMouseDown(previous.leftMouseIsDown, events)
    )

  private def lastMousePosition(previous: Point, events: List[MouseEvent]): Point =
    events.collect { case mp: MouseEvent.Move => mp.position }.reverse.headOption match {
      case None           => previous
      case Some(position) => position
    }

  private given CanEqual[List[MouseEvent], List[MouseEvent]] = CanEqual.derived

  @tailrec
  private def isLeftMouseDown(isDown: Boolean, events: List[MouseEvent]): Boolean =
    events match {
      case Nil =>
        isDown

      case MouseEvent.MouseDown(_, MouseButton.LeftMouseButton) :: xs =>
        isLeftMouseDown(true, xs)

      case MouseEvent.MouseUp(_, MouseButton.LeftMouseButton) :: xs =>
        isLeftMouseDown(false, xs)

      case _ :: xs =>
        isLeftMouseDown(isDown, xs)
    }
}
