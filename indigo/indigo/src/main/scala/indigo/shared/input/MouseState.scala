package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.MouseWheel
import indigo.shared.events.PointerId
import indigo.shared.events.PointerType

import scala.annotation.nowarn

@nowarn("msg=deprecated")
final case class MouseState(
    val instances: Batch[Mouse],
    @deprecated("Use `InputState.Wheel` instead", "0.22.0")
    val wheelEvents: Batch[MouseEvent.Wheel]
) extends ButtonInputState
    with PositionalInputState {
  @deprecated("This will soon be removed", "0.22.0")
  val pointerType: Option[PointerType] = Some(PointerType.Mouse)

  val primary   = instances.sortBy(_.pointerId.toDouble).headOption
  val pointerId = primary.map(_.pointerId).getOrElse(PointerId.unknown)
  val maybePosition =
    instances.sortBy(_.pointerId.toDouble).map(_.maybePosition).collect { case Some(p) => p }.headOption

  val buttons     = instances.flatMap(_.buttons)
  val clicks      = instances.flatMap(_.clicks)
  val downButtons = instances.flatMap(_.downButtons)
  val upButtons   = instances.flatMap(_.upButtons)

  lazy val isUpAt   = instances.flatMap(_.upButtons.map(_._2))
  lazy val isDownAt = instances.flatMap(_.downButtons.map(_._2))

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param position
    */
  def wasUpAt(position: Point): Boolean = wasButtonUpAt(position, MouseButton.LeftMouseButton)

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasUpAt(x: Int, y: Int): Boolean = wasUpAt(Point(x, y))

  /** Whether the pointer button was down at the specified position in this frame
    *
    * @param position
    * @return
    */
  def wasDownAt(position: Point): Boolean = wasButtonDownAt(position, MouseButton.LeftMouseButton)

  /** Whether the pointer button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasDownAt(x: Int, y: Int): Boolean = wasDownAt(Point(x, y))

  @deprecated("Use `MouseState.WasButtonUpWithin` instead", "0.22.0")
  def wasUpWithin(bounds: Rectangle, mouseButton: MouseButton): Boolean = wasButtonUpWithin(bounds, mouseButton)

  def wasUpWithin(bounds: Rectangle): Boolean = wasButtonUpWithin(bounds, MouseButton.LeftMouseButton)
  def wasUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasUpWithin(
    Rectangle(x, y, width, height)
  )

  @deprecated("Use `MouseState.WasButtonDownWithin` instead", "0.22.0")
  def wasDownWithin(bounds: Rectangle, mouseButton: MouseButton): Boolean = wasButtonDownWithin(bounds, mouseButton)

  def wasDownWithin(bounds: Rectangle): Boolean = wasButtonDownWithin(bounds, MouseButton.LeftMouseButton)
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasDownWithin(
    Rectangle(x, y, width, height)
  )

  @deprecated("Use `InputState.Wheel` instead", "0.22.0")
  lazy val scrolled: Option[MouseWheel] =

    @nowarn("msg=deprecated")
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[MouseWheel]
    else if amount < 0 then Some(MouseWheel.ScrollUp)
    else Some(MouseWheel.ScrollDown)

  @nowarn("msg=deprecated")
  def calculateNext(events: Batch[MouseEvent]) =
    val newInstances = events.foldLeft(
      instances.map(_.copy(upButtons = Batch.empty, downButtons = Batch.empty, clicks = Batch.empty))
    )((instances, event) =>
      if (event.isInstanceOf[MouseEvent.Leave] || event.isInstanceOf[MouseEvent.Cancel])
        instances.filterNot(_.pointerId == event.pointerId)
      else
        val instance = MouseState.getOrCreate(event.pointerId, instances)
        val newInstance = event match {
          case e: MouseEvent.Move  => instance.copy(maybePosition = Some(e.position))
          case e: MouseEvent.Enter => instance.copy(maybePosition = Some(e.position))
          case e: MouseEvent.Click => instance.copy(clicks = instance.clicks :+ (e.button, e.position))
          case e: MouseEvent.Down =>
            instance.copy(
              downButtons = instance.downButtons :+ (e.button, e.position),
              buttons = instance.buttons :+ e.button
            )
          case e: MouseEvent.Up =>
            instance.copy(
              upButtons = instance.upButtons :+ (e.button, e.position),
              buttons = instance.buttons.filterNot(_ == e.button)
            )
          // We should never reach here
          case _: MouseEvent.Leave => instance
          // We should never reach here
          case _: MouseEvent.Cancel => instance
          // Deprecated events
          case _: (MouseEvent.MouseDown | MouseEvent.MouseUp | MouseEvent.Wheel) => instance
        }

        instances.filterNot(_.pointerId == event.pointerId) :+ newInstance
    )

    this.copy(instances = newInstances, wheelEvents = events.collect { case e: MouseEvent.Wheel => e })
}

object MouseState:
  @nowarn("msg=deprecated")
  val default: MouseState = MouseState(Batch.empty, Batch.empty)

  private def getOrCreate(id: PointerId, instances: Batch[Mouse]) =
    instances
      .find(p => p.pointerId == id)
      .getOrElse(
        Mouse(
          id,
          None,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Batch.empty
        )
      )

final case class Mouse(
    pointerId: PointerId,
    maybePosition: Option[Point],
    buttons: Batch[MouseButton],
    clicks: Batch[(MouseButton, Point)],
    downButtons: Batch[(MouseButton, Point)],
    upButtons: Batch[(MouseButton, Point)]
)
