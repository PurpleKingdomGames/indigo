package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.PointerId
import indigo.shared.collections.Batch.toBatch
import indigo.MouseButton
import indigo.shared.events.PointerType

final class Pointers(
    private val pointerBatch: Batch[Pointer],
    private val pointerEvents: Batch[PointerEvent]
) {
  def isButtonDown(button: MouseButton): Boolean = pointerBatch.map(_.buttonsDown).contains(button)

  lazy val isLeftDown: Boolean   = isButtonDown(MouseButton.LeftMouseButton)
  lazy val isRightDown: Boolean  = isButtonDown(MouseButton.RightMouseButton)
  lazy val isMiddleDown: Boolean = isButtonDown(MouseButton.MiddleMouseButton)

  lazy val pointerPressed: Boolean  = pressed(MouseButton.LeftMouseButton)
  lazy val pointerReleased: Boolean = released(MouseButton.LeftMouseButton)

  lazy val pointersUpAt: Batch[Point]   = upAtPositionsWith(None)
  lazy val pointersDownAt: Batch[Point] = downAtPositionsWith(None)

  def pressed(button: MouseButton): Boolean =
    pointerEvents.exists {
      case md: PointerEvent.PointerDown if md.button == Some(button) => true
      case _                                                         => false
    }

  def released(button: MouseButton): Boolean =
    pointerEvents.exists {
      case mu: PointerEvent.PointerUp if mu.button == Some(button) => true
      case _                                                       => false
    }

  private def upAtPositionsWith(button: Option[MouseButton]): Batch[Point] = pointerEvents.collect {
    case m: PointerEvent.PointerUp if button == None || m.button == button => m.position
  }
  private def downAtPositionsWith(button: Option[MouseButton]): Batch[Point] = pointerEvents.collect {
    case m: PointerEvent.PointerDown if button == None || m.button == button => m.position
  }

  def wasPointerUpAt(position: Point): Boolean               = pointersUpAt.contains(position)
  def wasPointerUpAt(x: Int, y: Int): Boolean                = wasPointerUpAt(Point(x, y))
  def wasUpAt(position: Point, button: MouseButton): Boolean = upAtPositionsWith(Some(button)).contains(position)
  def wasUpAt(x: Int, y: Int, button: MouseButton): Boolean  = wasUpAt(Point(x, y), button)

  def wasPointerDownAt(position: Point): Boolean               = pointersDownAt.contains(position)
  def wasPointerDownAt(x: Int, y: Int): Boolean                = wasPointerDownAt(Point(x, y))
  def wasDownAt(position: Point, button: MouseButton): Boolean = downAtPositionsWith(Some(button)).contains(position)
  def wasDownAt(x: Int, y: Int, button: MouseButton): Boolean  = wasDownAt(Point(x, y), button)

  def wasPointerPositionAt(target: Point): Boolean  = pointerBatch.map(_.position).contains(target)
  def wasPointerPositionAt(x: Int, y: Int): Boolean = wasPointerPositionAt(Point(x, y))

  // Within
  private def wasPointerWithin(bounds: Rectangle, pt: Point): Boolean =
    bounds.isPointWithin(pt)

  def wasPointerUpWithin(bounds: Rectangle, button: MouseButton): Boolean =
    upAtPositionsWith(Some(button)).exists(wasPointerWithin(bounds, _))
  def wasPointerUpWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasPointerUpWithin(Rectangle(x, y, width, height), button)

  def wasPointerDownWithin(bounds: Rectangle): Boolean = downAtPositionsWith(None).exists(wasPointerWithin(bounds, _))
  def wasPointerDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasPointerDownWithin(
    Rectangle(x, y, width, height)
  )
  def wasPointerDownWithin(bounds: Rectangle, button: MouseButton): Boolean =
    downAtPositionsWith(Some(button)).exists(wasPointerWithin(bounds, _))
  def wasPointerDownWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasPointerDownWithin(Rectangle(x, y, width, height), button)

  def wasPointerPositionWithin(bounds: Rectangle): Boolean =
    pointerBatch.map(_.position).exists(bounds.isPointWithin(_))
  def wasPointerPositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasPointerPositionWithin(Rectangle(x, y, width, height))
}

object Pointers:
  val default: Pointers =
    Pointers(Batch.empty, Batch.empty)

  def calculateNext(previous: Pointers, events: Batch[PointerEvent]): Pointers =
    Pointers(updatePointers(events, previous), events)

  private def updatePointers(events: Batch[PointerEvent], previous: Pointers): Batch[Pointer] =
    val pointersToRemove = events
      .filter(_ match {
        case _: PointerEvent.PointerOut => true
        case _                          => false
      })
      .map(_.pointerId)

    var pointersToAdd = events
      .filter(_ match {
        case _: (PointerEvent.PointerCancel | PointerEvent.PointerOut) => false
        case e                                                         => true
      })
      .map(e => Pointer(e.pointerId, e.pointerType, e.buttons, e.position))

    previous.pointerBatch
      .filterNot(p => pointersToRemove.contains(p.id) || pointersToAdd.exists(_.id == p.id))
      ++ pointersToAdd

final case class Pointer(id: PointerId, pointerType: PointerType, buttonsDown: Batch[MouseButton], position: Point)
