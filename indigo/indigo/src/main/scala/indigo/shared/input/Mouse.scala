package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.MouseEvent
import indigo.shared.events.MouseWheel
import indigo.shared.events.PointerType

final class Mouse(val pointers: Pointers, val wheelEvents: Batch[MouseEvent.Wheel]) extends PointerState {
  val pointerType: Option[PointerType] = Some(PointerType.Mouse)

  @deprecated("Use `isClicked` instead.", "0.18.0")
  lazy val mouseClicked: Boolean = isClicked

  @deprecated("Use `isPressed` instead.", "0.18.0")
  lazy val mousePressed: Boolean = pressed(MouseButton.LeftMouseButton)

  @deprecated("Use `isReleased` instead.", "0.18.0")
  lazy val mouseReleased: Boolean = released(MouseButton.LeftMouseButton)

  lazy val scrolled: Option[MouseWheel] =
    val amount = wheelEvents.foldLeft(0d) { case (acc, e) =>
      acc + e.deltaY
    }

    if amount == 0 then Option.empty[MouseWheel]
    else if amount < 0 then Some(MouseWheel.ScrollUp)
    else Some(MouseWheel.ScrollDown)

  @deprecated("Use `isClickedAt` instead.", "0.18.0")
  lazy val mouseClickAt: Option[Point] = isClickedAt.headOption

  @deprecated("Use `isUpAt` instead.", "0.18.0")
  lazy val mouseUpAt: Option[Point] = isUpAt.headOption

  @deprecated("Use `isDownAt` instead.", "0.18.0")
  lazy val mouseDownAt: Option[Point] = isDownAt.headOption

  @deprecated("Use `wasClickedAt` instead.", "0.18.0")
  def wasMouseClickedAt(position: Point): Boolean = wasClickedAt(position)

  @deprecated("Use `wasClickedAt` instead.", "0.18.0")
  def wasMouseClickedAt(x: Int, y: Int): Boolean = wasClickedAt(Point(x, y))

  @deprecated("Use `wasUpAt` instead.", "0.18.0")
  def wasMouseUpAt(position: Point): Boolean = wasUpAt(position, MouseButton.LeftMouseButton)

  @deprecated("Use `wasUpAt` instead.", "0.18.0")
  def wasMouseUpAt(x: Int, y: Int): Boolean = wasUpAt(Point(x, y), MouseButton.LeftMouseButton)

  @deprecated("Use `wasDownAt` instead.", "0.18.0")
  def wasMouseDownAt(position: Point): Boolean = wasDownAt(position, MouseButton.LeftMouseButton)

  @deprecated("Use `wasDownAt` instead.", "0.18.0")
  def wasMouseDownAt(x: Int, y: Int): Boolean = wasDownAt(Point(x, y), MouseButton.LeftMouseButton)

  @deprecated("Use `wasPositionAt` instead.", "0.18.0")
  def wasMousePositionAt(target: Point): Boolean = wasPositionAt(target)

  @deprecated("Use `wasPositionAt` instead.", "0.18.0")
  def wasMousePositionAt(x: Int, y: Int): Boolean = wasPositionAt(Point(x, y))

  @deprecated("Use `wasClickedWithin` instead.", "0.18.0")
  def wasMouseClickedWithin(bounds: Rectangle): Boolean = wasClickedWithin(bounds, MouseButton.LeftMouseButton)

  @deprecated("Use `wasClickedWithin` instead.", "0.18.0")
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasClickedWithin(
    Rectangle(x, y, width, height),
    MouseButton.LeftMouseButton
  )

  @deprecated("Use `wasUpWithin` instead.", "0.18.0")
  def wasMouseUpWithin(bounds: Rectangle): Boolean = wasUpWithin(bounds, MouseButton.LeftMouseButton)

  @deprecated("Use `wasUpWithin` instead.", "0.18.0")
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasUpWithin(
    Rectangle(x, y, width, height),
    MouseButton.LeftMouseButton
  )

  @deprecated("Use `wasDownWithin` instead.", "0.18.0")
  def wasMouseDownWithin(bounds: Rectangle): Boolean = wasDownWithin(bounds, MouseButton.LeftMouseButton)

  @deprecated("Use `wasDownWithin` instead.", "0.18.0")
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasDownWithin(
    Rectangle(x, y, width, height),
    MouseButton.LeftMouseButton
  )

  @deprecated("Use `wasWithin` instead.", "0.18.0")
  def wasMousePositionWithin(bounds: Rectangle): Boolean = wasWithin(bounds)

  @deprecated("Use `wasWithin` instead.", "0.18.0")
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasWithin(Rectangle(x, y, width, height))
}
object Mouse:
  val default: Mouse = Mouse(Pointers.default, Batch.empty)
