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
) {

  /** Whether the specified button is down on any pointer
    *
    * @param button
    *   The button to check
    * @return
    */
  def isButtonDown(button: MouseButton): Boolean = pointerBatch.map(_.buttonsDown).contains(button)

  /** Whether the left button is down on any pointer
    */
  lazy val isLeftDown: Boolean = isButtonDown(MouseButton.LeftMouseButton)

  /** Whether the right button is down on any pointer
    */
  lazy val isRightDown: Boolean = isButtonDown(MouseButton.RightMouseButton)

  /** Whether the middle button is down on any pointer
    */
  lazy val isMiddleDown: Boolean = isButtonDown(MouseButton.MiddleMouseButton)

  /** Whether the left button was pressed in this frame
    */
  lazy val pointerPressed: Boolean = pressed(MouseButton.LeftMouseButton)

  /** Whether the left button was released in this frame
    */
  lazy val pointerReleased: Boolean = released(MouseButton.LeftMouseButton)

  /** All the positions where the pointers were up in this frame
    */
  lazy val pointersUpAt: Batch[Point] = upPositionsWith(None)

  /** All the positions where the pointers were down in this frame
    */
  lazy val pointersDownAt: Batch[Point] = downPositionsWith(None)

  /** Whether the left button was pressed in this frame
    *
    * @return
    */
  def pressed: Boolean = pressed(MouseButton.LeftMouseButton)

  /** Whether the specified button was pressed in this frame
    *
    * @param button
    *   The button to check
    * @return
    */
  def pressed(button: MouseButton): Boolean =
    pointerEvents.exists {
      case md: PointerEvent.PointerDown if md.button == Some(button) => true
      case _                                                         => false
    }

  /** Whether the left button was released in this frame
    *
    * @return
    */
  def released: Boolean = released(MouseButton.LeftMouseButton)

  /** Whether the specified button was released in this frame
    *
    * @param button
    *   The button to check
    * @return
    */
  def released(button: MouseButton): Boolean =
    pointerEvents.exists {
      case mu: PointerEvent.PointerUp if mu.button == Some(button) => true
      case _                                                       => false
    }

  /** All the positions where the specified button was up in this frame
    *
    * @param button
    */
  private def upPositionsWith(button: Option[MouseButton]): Batch[Point] = pointerEvents.collect {
    case m: PointerEvent.PointerUp if button == None || m.button == button => m.position
  }

  /** All the positions where the specified button was down in this frame
    *
    * @param button
    */
  private def downPositionsWith(button: Option[MouseButton]): Batch[Point] = pointerEvents.collect {
    case m: PointerEvent.PointerDown if button == None || m.button == button => m.position
  }

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param position
    */
  def wasPointerUpAt(position: Point): Boolean = pointersUpAt.contains(position)

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPointerUpAt(x: Int, y: Int): Boolean = wasPointerUpAt(Point(x, y))

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param position
    * @param button
    */
  def wasUpAt(position: Point, button: MouseButton): Boolean = upPositionsWith(Some(button)).contains(position)

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasUpAt(x: Int, y: Int, button: MouseButton): Boolean = wasUpAt(Point(x, y), button)

  /** Whether the pointer button was down at the specified position in this frame
    *
    * @param position
    * @return
    */
  def wasPointerDownAt(position: Point): Boolean = pointersDownAt.contains(position)

  /** Whether the pointer button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPointerDownAt(x: Int, y: Int): Boolean = wasPointerDownAt(Point(x, y))

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param position
    * @param button
    * @return
    */
  def wasDownAt(position: Point, button: MouseButton): Boolean = downPositionsWith(Some(button)).contains(position)

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasDownAt(x: Int, y: Int, button: MouseButton): Boolean = wasDownAt(Point(x, y), button)

  /** Whether the pointer position was at the specified target in this frame
    *
    * @param target
    * @return
    */
  def wasPointerPositionAt(target: Point): Boolean = pointerBatch.map(_.position).contains(target)

  /** Whether the pointer position was at the specified target in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPointerPositionAt(x: Int, y: Int): Boolean = wasPointerPositionAt(Point(x, y))

  // Within
  private def wasPointerWithin(bounds: Rectangle, pt: Point): Boolean =
    bounds.isPointWithin(pt)

  /** Whether the pointer was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def isPointerWithin(bounds: Rectangle) = getPointerWithin(bounds).nonEmpty

  /** All the pointers that were within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def getPointerWithin(bounds: Rectangle): Option[Point] = pointerBatch.map(_.position).find(bounds.isPointWithin)

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasPointerUpWithin(bounds: Rectangle, button: MouseButton): Boolean =
    upPositionsWith(Some(button)).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasPointerUpWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasPointerUpWithin(Rectangle(x, y, width, height), button)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasPointerDownWithin(bounds: Rectangle): Boolean = downPositionsWith(None).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasPointerDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasPointerDownWithin(
    Rectangle(x, y, width, height)
  )

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasPointerDownWithin(bounds: Rectangle, button: MouseButton): Boolean =
    downPositionsWith(Some(button)).exists(wasPointerWithin(bounds, _))

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasPointerDownWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasPointerDownWithin(Rectangle(x, y, width, height), button)

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasPointerPositionWithin(bounds: Rectangle): Boolean =
    pointerBatch.map(_.position).exists(bounds.isPointWithin(_))

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
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

    val pointersToAdd = events
      .filter(_ match {
        case _: (PointerEvent.PointerCancel | PointerEvent.PointerOut) => false
        case e                                                         => true
      })
      .map(e => Pointer(e.pointerId, e.pointerType, e.buttons, e.position))

    previous.pointerBatch
      .filterNot(p => pointersToRemove.contains(p.id) || pointersToAdd.exists(_.id == p.id))
      ++ pointersToAdd

final case class Pointer(id: PointerId, pointerType: PointerType, buttonsDown: Batch[MouseButton], position: Point)
