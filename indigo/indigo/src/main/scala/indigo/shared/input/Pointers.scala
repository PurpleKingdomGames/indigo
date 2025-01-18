package indigo.shared.input

import indigo.MouseButton
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.PointerId
import indigo.shared.events.PointerType

final class Pointers(
    private val pointerBatch: Batch[Pointer],
    val pointerEvents: Batch[PointerEvent]
) extends PointerState {
  val pointers: Pointers               = this
  val pointerType: Option[PointerType] = None

  def pointerPositions(pointerType: Option[PointerType]): Batch[Point] =
    pointersOfType(pointerType).map(_.position)

  /** Whether the specified button is down on any pointer
    *
    * @param button
    *   The button to check
    * @return
    */
  def isButtonDown(button: MouseButton, pointerType: Option[PointerType]): Boolean =
    pointersOfType(pointerType).flatMap(_.buttonsDown).contains(button)

  /** Whether the left button is down on any pointer
    */
  def isLeftDown(pointerType: Option[PointerType]): Boolean = isButtonDown(MouseButton.LeftMouseButton, pointerType)

  /** Whether the right button is down on any pointer
    */
  def isRightDown(pointerType: Option[PointerType]): Boolean = isButtonDown(MouseButton.RightMouseButton, pointerType)

  /** Whether the middle button is down on any pointer
    */
  def isMiddleDown(pointerType: Option[PointerType]): Boolean = isButtonDown(MouseButton.MiddleMouseButton, pointerType)

  /** Whether the left button was pressed in this frame
    */
  def pointerPressed(pointerType: Option[PointerType]): Boolean = pressed(MouseButton.LeftMouseButton, pointerType)

  /** Whether the left button was released in this frame
    */
  def pointerReleased(pointerType: Option[PointerType]): Boolean = released(MouseButton.LeftMouseButton, pointerType)

  def pointerClicked(pointerType: Option[PointerType]): Boolean = pointerEventsOfType(pointerType).exists {
    case _: PointerEvent.Click => true
    case _                     => false
  }

  def pointersClickedAt(pointerType: Option[PointerType]): Batch[Point] =
    pointerEventsOfType(pointerType)
      .filter(_ match {
        case _: PointerEvent.Click => true
        case _                     => false
      })
      .map(_.position)

  /** All the positions where the pointers were up in this frame
    */
  def pointersUpAt(pointerType: Option[PointerType]): Batch[Point] = upPositionsWith(None, pointerType)

  /** All the positions where the pointers were down in this frame
    */
  def pointersDownAt(pointerType: Option[PointerType]): Batch[Point] = downPositionsWith(None, pointerType)

  /** Whether the left button was pressed in this frame
    *
    * @return
    */
  def pressed(pointerType: Option[PointerType]): Boolean = pressed(MouseButton.LeftMouseButton, pointerType)

  /** Whether the specified button was pressed in this frame
    *
    * @param button
    *   The button to check
    * @return
    */
  def pressed(button: MouseButton, pointerType: Option[PointerType]): Boolean =
    pointerEventsOfType(pointerType)
      .exists {
        case md: PointerEvent.Down if md.button == Some(button) => true
        case _                                                  => false
      }

  /** Whether the specified button was released in this frame
    *
    * @param button
    *   The button to check
    * @return
    */
  def released(button: MouseButton, pointerType: Option[PointerType]): Boolean =
    pointerEventsOfType(pointerType)
      .exists {
        case mu: PointerEvent.Up if mu.button == Some(button) => true
        case _                                                => false
      }

  /** Was any pointer clicked at this position in this frame
    *
    * @param position
    * @return
    */
  def wasPointerClickedAt(position: Point, pointerType: Option[PointerType]): Boolean =
    pointersClickedAt(pointerType).contains(position)

  /** Was any pointer clicked at this position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPointerClickedAt(x: Int, y: Int, pointerType: Option[PointerType]): Boolean =
    wasPointerClickedAt(Point(x, y), pointerType)

  /** All the positions where the specified button was up in this frame
    *
    * @param button
    */
  def upPositionsWith(button: Option[MouseButton], pointerType: Option[PointerType]): Batch[Point] =
    pointerEventsOfType(pointerType).collect {
      case m: PointerEvent.Up if button == None || m.button == button => m.position
    }

  /** All the positions where the specified button was down in this frame
    *
    * @param button
    */
  def downPositionsWith(button: Option[MouseButton], pointerType: Option[PointerType]): Batch[Point] =
    pointerEventsOfType(pointerType).collect {
      case m: PointerEvent.Down if button == None || m.button == button => m.position
    }

  /** All the positions where the specified button was clicked in this frame
    *
    * @param button
    */
  def clickedPositionsWith(button: Option[MouseButton], pointerType: Option[PointerType]): Batch[Point] =
    pointerEventsOfType(pointerType).collect {
      case m: PointerEvent.Click if button == None || m.button == button => m.position
    }

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param position
    * @param button
    */
  def wasUpAt(position: Point, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    upPositionsWith(Some(button), pointerType).contains(position)

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasUpAt(x: Int, y: Int, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    wasUpAt(Point(x, y), button, pointerType)

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param position
    * @param button
    * @return
    */
  def wasDownAt(position: Point, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    downPositionsWith(Some(button), pointerType).contains(position)

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasDownAt(x: Int, y: Int, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    wasDownAt(Point(x, y), button, pointerType)

  /** Whether the pointer position was at the specified target in this frame
    *
    * @param target
    * @return
    */
  def wasPointerPositionAt(target: Point, pointerType: Option[PointerType]): Boolean =
    pointersOfType(pointerType).map(_.position).contains(target)

  /** Whether the pointer position was at the specified target in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPointerPositionAt(x: Int, y: Int, pointerType: Option[PointerType]): Boolean =
    wasPointerPositionAt(Point(x, y), pointerType)

  // Within
  private def wasPointerWithin(bounds: Rectangle, pt: Point): Boolean =
    bounds.isPointWithin(pt)

  /** Whether the pointer was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def isPointerWithin(bounds: Rectangle, pointerType: Option[PointerType]) =
    getPointerWithin(bounds, pointerType).nonEmpty

  /** All the pointers that were within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def getPointerWithin(bounds: Rectangle, pointerType: Option[PointerType]): Option[Point] =
    pointersOfType(pointerType).map(_.position).find(bounds.isPointWithin)

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasPointerUpWithin(bounds: Rectangle, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    upPositionsWith(Some(button), pointerType).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasPointerUpWithin(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      button: MouseButton,
      pointerType: Option[PointerType]
  ): Boolean =
    wasPointerUpWithin(Rectangle(x, y, width, height), button, pointerType)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasPointerDownWithin(bounds: Rectangle, pointerType: Option[PointerType]): Boolean =
    downPositionsWith(None, pointerType).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasPointerDownWithin(x: Int, y: Int, width: Int, height: Int, pointerType: Option[PointerType]): Boolean =
    wasPointerDownWithin(
      Rectangle(x, y, width, height),
      pointerType
    )

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasPointerDownWithin(bounds: Rectangle, button: MouseButton, pointerType: Option[PointerType]): Boolean =
    downPositionsWith(Some(button), pointerType).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasPointerDownWithin(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      button: MouseButton,
      pointerType: Option[PointerType]
  ): Boolean =
    wasPointerDownWithin(Rectangle(x, y, width, height), button, pointerType)

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasPointerPositionWithin(bounds: Rectangle, pointerType: Option[PointerType]): Boolean =
    pointersOfType(pointerType).map(_.position).exists(bounds.isPointWithin(_))

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasPointerPositionWithin(x: Int, y: Int, width: Int, height: Int, pointerType: Option[PointerType]): Boolean =
    wasPointerPositionWithin(Rectangle(x, y, width, height), pointerType)

  private def pointersOfType(pointerType: Option[PointerType]): Batch[Pointer] =
    pointerType match {
      case Some(t) =>
        pointerBatch.filter(_.pointerType == t)
      case None => pointerBatch
    }

  private def pointerEventsOfType(pointerType: Option[PointerType]): Batch[PointerEvent] =
    pointerType match {
      case Some(t) =>
        pointerEvents.filter(_.pointerType == t)
      case None => pointerEvents
    }
}

object Pointers:
  val default: Pointers =
    Pointers(Batch.empty, Batch.empty)

  def calculateNext(previous: Pointers, events: Batch[PointerEvent]): Pointers =
    Pointers(updatePointers(events, previous), events)

  private def updatePointers(events: Batch[PointerEvent], previous: Pointers): Batch[Pointer] =
    val pointersToRemove = events
      .filter(_ match {
        case _: PointerEvent.Out => true
        case _                   => false
      })
      .map(_.pointerId)

    val pointersToAdd = Batch.fromArray(
      events
        .filter(_ match {
          case _: (PointerEvent.Cancel | PointerEvent.Out) => false
          case e                                           => true
        })
        .foldLeft(Map.empty[PointerId, Pointer])((acc, e) =>
          acc + (e.pointerId -> Pointer(e.pointerId, e.pointerType, e.buttons, e.position))
        )
        .values
        .toArray
    )

    previous.pointerBatch
      .filterNot(p => pointersToRemove.contains(p.id) || pointersToAdd.exists(_.id == p.id))
      ++ pointersToAdd

final case class Pointer(id: PointerId, pointerType: PointerType, buttonsDown: Batch[MouseButton], position: Point)
object Pointer:
  def apply(position: Point, pointerType: PointerType): Pointer =
    Pointer(PointerId(0), pointerType, Batch.empty, position)
  def apply(position: Point, pointerType: PointerType, button: MouseButton): Pointer =
    Pointer(PointerId(0), pointerType, Batch(button), position)
