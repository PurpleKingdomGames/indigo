package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.PointerId
import indigo.shared.collections.Batch.toBatch
import indigo.MouseButton

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
}

object Pointers:
  val default: Pointers =
    Pointers(Batch.empty, Batch.empty)

  def calculateNext(previous: Pointers, events: Batch[PointerEvent]): Pointers =
    Pointers(updatePointers(events, previous), events)

  private def updatePointers(events: Batch[PointerEvent], previous: Pointers): Batch[Pointer] =
    events
      .map(_ match {
        case e: PointerEvent.PointerCancel => None
        case e                             => Some(e)
      })
      .collect { case Some(e) => e }
      .map(e => updatePointer(e, previous.pointerBatch.find(_.id == e.pointerId)))

  private def updatePointer(event: PointerEvent, previous: Option[Pointer]): Pointer =
    previous match
      case None =>
        Pointer(
          event.pointerId,
          event match {
            case e: PointerEvent.PointerDown => e.buttons
            case _                           => Batch.empty
          },
          event.position
        )

      case Some(p) =>
        Pointer(
          p.id,
          event.buttons,
          event.position
        )

final case class Pointer(id: PointerId, buttonsDown: Batch[MouseButton], position: Point)
