package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.FingerId
import indigo.shared.events.PointerId
import indigo.shared.events.TouchEvent

final case class TouchState(fingers: Batch[Finger]) extends PositionalInputState {
  val primary = fingers.sortBy(_.pointerId.toDouble).headOption

  val pointerId = primary.map(_.pointerId).getOrElse(PointerId.unknown)
  val fingerId  = primary.map(_.fingerId).getOrElse(FingerId.unknown)

  val maybePosition = fingers.sortBy(_.pointerId.toDouble).map(_.maybePosition).collect { case Some(p) => p }.headOption
  val isDown        = fingers.exists(_.isDown)
  val isUp          = isDown == false && fingers.exists(_.isUp)
  val isTap         = fingers.exists(_.isTap)
  val pressure      = fingers.sortBy(_.pressure).headOption.map(_.pressure).getOrElse(0.0)

  def wasDownAt(position: Point): Boolean =
    fingers.exists(f => f.isDown && f.position == position)

  def wasDownWithin(rectangle: Rectangle): Boolean =
    fingers.exists(f => f.isDown && rectangle.contains(f.position))

  def wasDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasDownWithin(Rectangle(x, y, width, height))

  def wasUpAt(position: Point): Boolean =
    fingers.exists(f => f.isUp && f.position == position)

  def wasUpWithin(rectangle: Rectangle): Boolean =
    fingers.exists(f => f.isUp && rectangle.contains(f.position))

  def wasUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasUpWithin(Rectangle(x, y, width, height))

  def calculateNext(events: Batch[TouchEvent]) =
    val newFingers = events.foldLeft(
      fingers.map(_.copy(isUp = false, isDown = false, isTap = false))
    )((fingers, event) =>
      if (event.isInstanceOf[TouchEvent.Leave] || event.isInstanceOf[TouchEvent.Cancel])
        fingers.filterNot(_.fingerId == event.fingerId)
      else
        val finger = TouchState.getOrCreate(event.pointerId, event.fingerId, fingers)
        val newFinger = event match {
          case e: TouchEvent.Move  => finger.copy(maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Enter => finger.copy(maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Tap => finger.copy(isTap = true, maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Down =>
            finger.copy(isDown = true, maybePosition = Option(e.position), pressure = e.pressure)
          case e: TouchEvent.Up => finger.copy(isUp = true, maybePosition = Option(e.position), pressure = e.pressure)
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
      .getOrElse(Finger(pointer, id, None, false, false, false, 0.0))

final case class Finger(
    pointerId: PointerId,
    fingerId: FingerId,
    maybePosition: Option[Point],
    isDown: Boolean,
    isUp: Boolean,
    isTap: Boolean,
    pressure: Double
) extends PositionalInputState
