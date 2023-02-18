package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.MouseWheel

import scala.annotation.tailrec

final class Mouse(
    mouseEvents: Batch[MouseEvent],
    val position: Point,
    @deprecated("use `isButtonDown` function instead of this value", "0.12.0") val leftMouseIsDown: Boolean,
    val buttonsDown: Set[MouseButton]
) {

  def this(mouseEvents: Batch[MouseEvent], position: Point, leftMouseIsDown: Boolean) =
    this(
      mouseEvents,
      position,
      leftMouseIsDown,
      buttonsDown = if (leftMouseIsDown) Set(MouseButton.LeftMouseButton) else Set.empty
    )

  def isButtonDown(button: MouseButton): Boolean = buttonsDown.contains(button)

  lazy val isLeftDown: Boolean  = isButtonDown(MouseButton.LeftMouseButton)
  lazy val isRightDown: Boolean = isButtonDown(MouseButton.RightMouseButton)

  lazy val mouseClicked: Boolean = mouseEvents.exists {
    case _: MouseEvent.Click => true
    case _                   => false
  }
  lazy val mousePressed  = pressed(MouseButton.LeftMouseButton)
  lazy val mouseReleased = released(MouseButton.LeftMouseButton)

  def pressed(button: MouseButton): Boolean =
    mouseEvents.exists {
      case md: MouseEvent.MouseDown if md.button == button => true
      case _                                               => false
    }

  def released(button: MouseButton): Boolean =
    mouseEvents.exists {
      case mu: MouseEvent.MouseUp if mu.button == button => true
      case _                                             => false
    }

  lazy val scrolled: Option[MouseWheel] =
    val amount = mouseEvents.foldLeft(0d) {
      case (acc, MouseEvent.Wheel(_, deltaY)) => acc + deltaY
      case (acc, _)                           => acc
    }

    if amount == 0 then Option.empty[MouseWheel]
    else if amount < 0 then Some(MouseWheel.ScrollUp)
    else Some(MouseWheel.ScrollDown)

  lazy val mouseClickAt: Option[Point] = mouseEvents.collectFirst { case m: MouseEvent.Click =>
    m.position
  }
  lazy val mouseUpAt: Option[Point]   = maybeUpAtPositionWith(MouseButton.LeftMouseButton)
  lazy val mouseDownAt: Option[Point] = maybeDownAtPositionWith(MouseButton.LeftMouseButton)

  def maybeUpAtPositionWith(button: MouseButton): Option[Point] = mouseEvents.collectFirst {
    case m: MouseEvent.MouseUp if m.button == button => m.position
  }
  def maybeDownAtPositionWith(button: MouseButton): Option[Point] = mouseEvents.collectFirst {
    case m: MouseEvent.MouseDown if m.button == button => m.position
  }

  private def wasMouseAt(position: Point, maybePosition: Option[Point]): Boolean =
    maybePosition match
      case Some(pt) => position == pt
      case None     => false

  def wasMouseClickedAt(position: Point): Boolean = wasMouseAt(position, mouseClickAt)
  def wasMouseClickedAt(x: Int, y: Int): Boolean  = wasMouseClickedAt(Point(x, y))

  def wasMouseUpAt(position: Point): Boolean = wasMouseAt(position, mouseUpAt)
  def wasMouseUpAt(x: Int, y: Int): Boolean  = wasMouseUpAt(Point(x, y))
  def wasUpAt(position: Point, button: MouseButton): Boolean =
    wasMouseAt(position, maybeUpAtPositionWith(button))
  def wasUpAt(x: Int, y: Int, button: MouseButton): Boolean =
    wasUpAt(Point(x, y), button)

  def wasMouseDownAt(position: Point): Boolean = wasMouseAt(position, mouseDownAt)
  def wasMouseDownAt(x: Int, y: Int): Boolean  = wasMouseDownAt(Point(x, y))
  def wasDownAt(position: Point, button: MouseButton): Boolean =
    wasMouseAt(position, maybeDownAtPositionWith(button))
  def wasDownAt(x: Int, y: Int, button: MouseButton): Boolean =
    wasDownAt(Point(x, y), button)

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
  def wasUpWithin(bounds: Rectangle, button: MouseButton): Boolean =
    wasMouseWithin(bounds, maybeUpAtPositionWith(button))
  def wasUpWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasUpWithin(Rectangle(x, y, width, height), button)

  def wasMouseDownWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseDownAt)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseDownWithin(
    Rectangle(x, y, width, height)
  )
  def wasDownWithin(bounds: Rectangle, button: MouseButton): Boolean =
    wasMouseWithin(bounds, maybeDownAtPositionWith(button))
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasDownWithin(Rectangle(x, y, width, height), button)

  def wasMousePositionWithin(bounds: Rectangle): Boolean = bounds.isPointWithin(position)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMousePositionWithin(Rectangle(x, y, width, height))

}
object Mouse:
  val default: Mouse =
    Mouse(Batch.empty, Point.zero, false)

  def calculateNext(previous: Mouse, events: Batch[MouseEvent]): Mouse =
    val newButtonsDown = calculateButtonsDown(events, previous.buttonsDown)
    Mouse(
      events,
      lastMousePosition(previous.position, events),
      newButtonsDown.contains(MouseButton.LeftMouseButton),
      newButtonsDown
    )

  private def lastMousePosition(previous: Point, events: Batch[MouseEvent]): Point =
    events.collect { case mp: MouseEvent.Move => mp.position }.lastOption.fold(previous)(identity)

  private given CanEqual[Batch[MouseEvent], Batch[MouseEvent]] = CanEqual.derived

  // Similar strategy as followed for `Keyboard`, this one uses Set (no button order preserved)
  private def calculateButtonsDown(
      mouseEvents: Batch[MouseEvent],
      previousButtonsDown: Set[MouseButton]
  ): Set[MouseButton] =
    @tailrec
    def rec(remaining: List[MouseEvent], buttonsDownAcc: Set[MouseButton]): Set[MouseButton] =
      remaining match
        case Nil =>
          buttonsDownAcc
        case MouseEvent.MouseDown(_, button) :: moreEvents =>
          rec(moreEvents, buttonsDownAcc + button)
        case MouseEvent.MouseUp(_, button) :: moreEvents =>
          rec(moreEvents, buttonsDownAcc - button)
        case _ :: moreEvents =>
          rec(moreEvents, buttonsDownAcc)

    rec(mouseEvents.toList, previousButtonsDown)
