package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.PenEvent
import indigo.shared.events.PointerId

final case class PenState(instances: Batch[Pen]) extends ButtonInputState with PositionalInputState:
  /** The primary pen instance
    */
  val primary = instances.sortBy(_.pointerId.toDouble).headOption

  /** The unique identifier for the primary pen instance, or PointerId.unknown if no primary instance exists
    */
  val pointerId = primary.map(_.pointerId).getOrElse(PointerId.unknown)

  /** The position of the primary pen instance, or None if no primary instance exists
    */
  val maybePosition = primary.flatMap(_.maybePosition)

  /** The buttons that are currently pressed down on the pen
    */
  val buttons = instances.flatMap(_.buttons)

  /** The pen button clicks that have occurred in this frame
    */
  val clicks = instances.flatMap(_.clicks)

  /** The buttons that were pressed down in this frame
    */
  val downButtons = instances.flatMap(_.downButtons)

  /** The buttons that were released in this frame
    */
  val upButtons = instances.flatMap(_.upButtons)

  /** A batch of positions where the pen was up in this frame
    */
  lazy val isUpAt = instances.filter(_.isDown == false).map(_.maybePosition).collect { case Some(p) => p }

  /** A batch of positions where the pen was down in this frame
    */
  lazy val isDownAt = instances.filter(_.isDown).map(_.maybePosition).collect { case Some(p) => p }

  /** Whether the pen itself (not the buttons) was down in this frame
    */
  val isDown = instances.exists(_.isDown)

  /** Whether the pen itself (not the buttons) was tapped in this frame
    */
  val isTap = instances.exists(_.isTap)

  /** The pressure of the pen in this frame, which is the average pressure of all available pens (usually just 1)
    */
  val pressure = if instances.isEmpty then 0.0 else instances.map(_.pressure).sum / instances.size.toDouble

  /** Whether the pen was up at the specified position in this frame
    *
    * @param position
    *   The position to check
    * @return
    */
  def wasUpAt(position: Point): Boolean =
    instances.exists(pen => pen.isDown == false && pen.maybePosition == Some(position))

  /** Whether the pen was up at the specified position in this frame
    *
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasUpAt(x: Int, y: Int): Boolean = wasUpAt(Point(x, y))

  /** Whether the pen was down at the specified position in this frame
    *
    * @param position
    *   The position to check
    * @return
    */
  def wasDownAt(position: Point): Boolean = instances.exists(pen => pen.isDown && pen.maybePosition == Some(position))

  /** Whether the pen was down at the specified position in this frame
    *
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasDownAt(x: Int, y: Int): Boolean = wasDownAt(Point(x, y))

  /** Whether the pen was up within the specified bounds in this frame
    *
    * @param bounds
    *   The bounds to check
    * @return
    */
  def wasUpWithin(bounds: Rectangle): Boolean =
    instances.exists(pen => pen.isDown == false && pen.maybePosition.map(p => bounds.contains(p)).getOrElse(false))

  /** Whether the pen was up within the specified bounds in this frame
    * @param x
    *   The x coordinate of the top-left corner of the bounds
    * @param y
    *   The y coordinate of the top-left corner of the bounds
    * @param width
    *   The width of the bounds
    * @param height
    *   The height of the bounds
    * @return
    */
  def wasUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasUpWithin(Rectangle(x, y, width, height))

  /** Whether the pen was down within the specified bounds in this frame
    *
    * @param bounds
    *   The bounds to check
    * @return
    */
  def wasDownWithin(bounds: Rectangle): Boolean =
    instances.exists(pen => pen.isDown && pen.maybePosition.map(p => bounds.contains(p)).getOrElse(false))

  /** Whether the pen was down within the specified bounds in this frame
    * @param x
    *   The x coordinate of the top-left corner of the bounds
    * @param y
    *   The y coordinate of the top-left corner of the bounds
    * @param width
    *   The width of the bounds
    * @param height
    *   The height of the bounds
    * @return
    */
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasDownWithin(
    Rectangle(x, y, width, height)
  )

  def calculateNext(events: Batch[PenEvent]) =
    val newInstances = events.foldLeft(
      // Reset the frame state for all pen instances
      instances.map(
        _.copy(
          upButtons = Batch.empty,
          downButtons = Batch.empty,
          clicks = Batch.empty,
          isDown = false,
          isTap = false
        )
      )
    )((instances, event) =>
      // If the event is a leave or cancel, remove the instance with the matching pointerId
      if (event.isInstanceOf[PenEvent.Leave] || event.isInstanceOf[PenEvent.Cancel])
        instances.filterNot(_.pointerId == event.pointerId)
      else
        // Otherwise, find or create the instance for the pointerId and update it based on the event
        val instance = PenState.getOrCreate(event.pointerId, instances)
        val newPen = event match {
          case e: PenEvent.Move  => instance.copy(maybePosition = Some(e.position), pressure = e.pressure)
          case e: PenEvent.Enter => instance.copy(maybePosition = Some(e.position), pressure = e.pressure)
          // If the click contains a button, then we treat it as a button click
          case e: PenEvent.Click if e.button.isDefined =>
            instance.copy(
              maybePosition = Some(e.position),
              pressure = e.pressure,
              clicks = instance.clicks :+ (e.button.get, e.position)
            )
          // If the click does not contain a button, then we treat it as a tap
          case e: PenEvent.Click =>
            instance.copy(maybePosition = Some(e.position), pressure = e.pressure, isTap = true)
          // If the down event contains a button, then we treat it as a button down
          case e: PenEvent.Down if e.button.isDefined =>
            instance.copy(
              maybePosition = Some(e.position),
              pressure = e.pressure,
              downButtons = instance.downButtons :+ (e.button.get, e.position),
              buttons = instance.buttons :+ e.button.get
            )
          // If the down event does not contain a button, then we treat it as a pen down
          case e: PenEvent.Down =>
            instance.copy(isDown = true, maybePosition = Some(e.position), pressure = e.pressure)
          // If the up event contains a button, then we treat it as a button up
          case e: PenEvent.Up if e.button.isDefined =>
            instance.copy(
              maybePosition = Some(e.position),
              pressure = e.pressure,
              upButtons = instance.upButtons :+ (e.button.get, e.position),
              buttons = instance.buttons.filterNot(_ == e.button.get)
            )
          // If the up event does not contain a button, then we treat it as a pen up
          case e: PenEvent.Up =>
            instance.copy(isDown = false, maybePosition = Some(e.position), pressure = e.pressure)
          // We should never reach here
          case _: PenEvent.Leave => instance
          // We should never reach here
          case _: PenEvent.Cancel => instance
        }

        instances.filterNot(_.pointerId == event.pointerId) :+ newPen
    )

    this.copy(instances = newInstances)

object PenState:
  val default: PenState = PenState(Batch.empty)

  private def getOrCreate(id: PointerId, instances: Batch[Pen]) =
    instances
      .find(p => p.pointerId == id)
      .getOrElse(
        Pen(
          id,
          None,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          isDown = false,
          isTap = false,
          pressure = 0.0
        )
      )

final case class Pen(
    pointerId: PointerId,
    maybePosition: Option[Point],
    buttons: Batch[MouseButton],
    clicks: Batch[(MouseButton, Point)],
    downButtons: Batch[(MouseButton, Point)],
    upButtons: Batch[(MouseButton, Point)],
    isDown: Boolean,
    isTap: Boolean,
    pressure: Double
)
