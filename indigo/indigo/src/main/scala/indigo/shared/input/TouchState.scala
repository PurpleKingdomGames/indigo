package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.FingerId
import indigo.shared.events.PointerId
import indigo.shared.events.TouchEvent

final case class TouchState(fingers: Batch[Finger]) extends PositionalInputState {

  /** The primary finger instance, which is the one with the lowest pointerId
    */
  val primary = fingers.sortBy(_.pointerId.toDouble).headOption

  /** The unique identifier for the primary finger instance, or PointerId.unknown if no primary instance exists
    */
  val pointerId = primary.map(_.pointerId).getOrElse(PointerId.unknown)

  /** The unique identifier for the primary finger instance
    */
  val fingerId = primary.map(_.fingerId).getOrElse(FingerId.unknown)

  /** The position of the primary finger instance, or None if no primary instance exists
    */
  val maybePosition = primary.flatMap(_.maybePosition)

  /** Whether at least 1 finger is down in this frame
    */
  val isDown = fingers.exists(_.isDown)

  /** Whether at least 1 finger was tapped in this frame
    */
  val isTapped = fingers.exists(_.isTapped)

  /** The pressure of the touches in this frame, which is the average pressure of all available touches
    */
  val pressure = if fingers.isEmpty then 0.0 else fingers.map(_.pressure).sum / fingers.size.toDouble

  /** The positions of fingers that are currently up in this frame
    */
  lazy val isUpAt = fingers.filter(_.isDown == false).map(_.maybePosition).collect { case Some(p) => p }

  /** The positions of fingers that are currently down in this frame
    */
  lazy val isDownAt = fingers.filter(_.isDown).map(_.maybePosition).collect { case Some(p) => p }

  /** Whether a finger was tapped at the specified position in this frame
    *
    * @param position
    *   The position to check
    * @return
    */
  def wasTappedAt(position: Point): Boolean =
    fingers.exists(f => f.isTapped && f.maybePosition.contains(position))

  /** Whether a finger was tapped at the specified position in this frame
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasTappedAt(x: Int, y: Int): Boolean = wasTappedAt(Point(x, y))

  /** Whether a finger was tapped within the specified rectangle in this frame
    *
    * @param rectangle
    *   The rectangle to check
    * @return
    */
  def wasTappedWithin(rectangle: Rectangle): Boolean =
    fingers.exists(f => f.isTapped && f.maybePosition.exists(rectangle.contains))

  /** Whether a finger was tapped within the specified rectangle in this frame
    * @param x
    *   The x coordinate of the rectangle to check
    * @param y
    *   The y coordinate of the rectangle to check
    * @param width
    *   The width of the rectangle to check
    * @param height
    *   The height of the rectangle to check
    * @return
    */
  def wasTappedWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasTappedWithin(Rectangle(x, y, width, height))

  /** Whether a finger was down at the specified position in this frame
    * @param position
    *   The position to check
    * @return
    */
  def wasDownAt(position: Point): Boolean =
    fingers.exists(f => f.isDown && f.position == position)

  /** Whether a finger was down at the specified position in this frame
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasDownAt(x: Int, y: Int): Boolean = wasDownAt(Point(x, y))

  /** Whether a finger was down within the specified rectangle in this frame
    *
    * @param rectangle
    *   The rectangle to check
    * @return
    */
  def wasDownWithin(rectangle: Rectangle): Boolean =
    fingers.exists(f => f.isDown && rectangle.contains(f.position))

  /** Whether a finger was down within the specified rectangle in this frame
    * @param x
    *   The x coordinate of the rectangle to check
    * @param y
    *   The y coordinate of the rectangle to check
    * @param width
    *   The width of the rectangle to check
    * @param height
    *   The height of the rectangle to check
    * @return
    */
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasDownWithin(Rectangle(x, y, width, height))

  /** Whether a finger was up at the specified position in this frame
    * @param position
    *   The position to check
    * @return
    */
  def wasUpAt(position: Point): Boolean =
    fingers.exists(f => f.isDown == false && f.position == position)

  /** Whether a finger was up at the specified position in this frame
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasUpAt(x: Int, y: Int): Boolean = wasUpAt(Point(x, y))

  /** Whether a finger was up within the specified rectangle in this frame
    *
    * @param rectangle
    *   The rectangle to check
    * @return
    */
  def wasUpWithin(rectangle: Rectangle): Boolean =
    fingers.exists(f => f.isDown == false && rectangle.contains(f.position))

  /** Whether a finger was up within the specified rectangle in this frame
    * @param x
    *   The x coordinate of the rectangle to check
    * @param y
    *   The y coordinate of the rectangle to check
    * @param width
    *   The width of the rectangle to check
    * @param height
    *   The height of the rectangle to check
    * @return
    */
  def wasUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasUpWithin(Rectangle(x, y, width, height))

  def calculateNext(events: Batch[TouchEvent]) =
    val newFingers = events.foldLeft(
      // Reset the frame state for all fingers
      fingers.map(_.copy(isDown = false, isTapped = false))
    )((fingers, event) =>
      // If the event is a leave or cancel, we remove the finger from the state
      if (event.isInstanceOf[TouchEvent.Leave] || event.isInstanceOf[TouchEvent.Cancel])
        fingers.filterNot(_.fingerId == event.fingerId)
      else
        // Otherwise, find or create the finger for the pointerId and fingerId and update it based on the event
        val finger = TouchState.getOrCreate(event.pointerId, event.fingerId, fingers)
        val newFinger = event match {
          case e: TouchEvent.Move  => finger.copy(maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Enter => finger.copy(maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Tap =>
            finger.copy(isTapped = true, maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Down =>
            finger.copy(isDown = true, maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Up =>
            finger.copy(isDown = false, maybePosition = Option(e.position), pressure = e.pressure)
          // We should never reach here
          case _: TouchEvent.Leave => finger
          // We should never reach here
          case _: TouchEvent.Cancel => finger
        }

        fingers.filterNot(_.fingerId == event.fingerId) :+ newFinger
    )

    this.copy(fingers = newFingers)
}

object TouchState:
  val default: TouchState = TouchState(Batch.empty)

  private def getOrCreate(pointer: PointerId, id: FingerId, instances: Batch[Finger]) =
    instances
      .find(p => p.pointerId == pointer && p.fingerId == id)
      .getOrElse(Finger(pointer, id, None, false, false, 0.0))

final case class Finger(
    pointerId: PointerId,
    fingerId: FingerId,
    maybePosition: Option[Point],
    isDown: Boolean,
    isTapped: Boolean,
    pressure: Double
) extends PositionalInputState
