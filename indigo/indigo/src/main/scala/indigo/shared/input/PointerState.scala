package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton
import indigo.shared.events.PointerType

trait PointerState:
  val pointers: Pointers
  val pointerType: Option[PointerType]

  /** Whether the specified button is down on any pointer
    *
    * @param button
    *   The button to check
    * @return
    */
  def isButtonDown(button: MouseButton): Boolean = pointers.isButtonDown(button, pointerType)

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
  lazy val isPressed: Boolean = pressed(MouseButton.LeftMouseButton)

  /** Whether the left button was released in this frame
    */
  lazy val isReleased: Boolean = released(MouseButton.LeftMouseButton)

  /** Thw positions that the pointer was clicked at in this frame
    */
  lazy val isClickedAt: Batch[Point] = pointers.pointersClickedAt(pointerType)

  /** The positions that the pointer was up at in this frame
    */
  lazy val isUpAt: Batch[Point] = pointers.upPositionsWith(Some(MouseButton.LeftMouseButton), pointerType)

  /** The positions that the pointer was down at in this frame
    */
  lazy val isDownAt: Batch[Point] = pointers.downPositionsWith(Some(MouseButton.LeftMouseButton), pointerType)

  /** The positions of the pointer in this frame
    */
  lazy val positions: Batch[Point] = pointers.pointerPositions(pointerType)

  /** The first position of the pointer in this frame, defaulting to 0,0
    */
  lazy val position: Point = maybePosition.getOrElse(Point.zero)

  /** The first position of the pointer in this frame
    */
  lazy val maybePosition: Option[Point] = positions.headOption

  /** Whether the pointer was clicked this frame
    */
  lazy val isClicked: Boolean = pointers.pointerClicked(pointerType)

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
  def pressed(button: MouseButton): Boolean = pointers.pressed(button, pointerType)

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
  def released(button: MouseButton): Boolean = pointers.released(button, pointerType)

  /** The first position where the specified button was up on this frame
    *
    * @param button
    * @return
    */
  def maybeUpAtPositionWith(button: MouseButton): Option[Point] =
    pointers.upPositionsWith(Some(button), pointerType).headOption

  /** The first position where the specified button was down on this frame
    *
    * @param button
    * @return
    */
  def maybeDownAtPositionWith(button: MouseButton): Option[Point] =
    pointers.downPositionsWith(Some(button), pointerType).headOption

  /** Was any pointer clicked at this position in this frame
    *
    * @param position
    * @return
    */
  def wasClickedAt(position: Point): Boolean = isClickedAt.contains(position)

  /** Was any pointer clicked at this position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasClickedAt(x: Int, y: Int): Boolean = wasClickedAt(Point(x, y))

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param position
    */
  def wasUpAt(position: Point): Boolean = pointers.pointersUpAt(pointerType).contains(position)

  /** Whether the pointer button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasUpAt(x: Int, y: Int): Boolean = wasUpAt(Point(x, y))

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param position
    * @param button
    */
  def wasUpAt(position: Point, button: MouseButton): Boolean =
    pointers.upPositionsWith(Some(button), pointerType).contains(position)

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
  def wasDownAt(position: Point): Boolean = pointers.pointersDownAt(pointerType).contains(position)

  /** Whether the pointer button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasDownAt(x: Int, y: Int): Boolean = wasDownAt(Point(x, y))

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param position
    * @param button
    * @return
    */
  def wasDownAt(position: Point, button: MouseButton): Boolean =
    pointers.downPositionsWith(Some(button), pointerType).contains(position)

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
  def wasPositionAt(target: Point): Boolean = pointers.wasPointerPositionAt(target, pointerType)

  /** Whether the pointer position was at the specified target in this frame
    *
    * @param x
    * @param y
    * @return
    */
  def wasPositionAt(x: Int, y: Int): Boolean = wasPositionAt(Point(x, y))

  /** Whether the pointer was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def isWithin(bounds: Rectangle) = pointers.getPointerWithin(bounds, pointerType).nonEmpty

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasUpWithin(bounds: Rectangle, button: MouseButton): Boolean =
    pointers.wasPointerUpWithin(bounds, button, pointerType)

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasUpWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasUpWithin(Rectangle(x, y, width, height), button)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasDownWithin(bounds: Rectangle): Boolean = pointers.wasPointerDownWithin(bounds, pointerType)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasDownWithin(
    Rectangle(x, y, width, height)
  )

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasDownWithin(bounds: Rectangle, button: MouseButton): Boolean = pointers.wasPointerDownWithin(
    bounds,
    button,
    pointerType
  )

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasDownWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    pointers.wasPointerDownWithin(
      Rectangle(x, y, width, height),
      button,
      pointerType
    )

  /** Whether the pointer was clicked within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasClickedWithin(bounds: Rectangle): Boolean = wasClickedWithin(bounds, None)

  /** Whether the pointer button was clicked within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasClickedWithin(bounds: Rectangle, button: MouseButton): Boolean = wasClickedWithin(bounds, Some(button))

  /** Whether the pointer was clicked within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasClickedWithin(
    Rectangle(x, y, width, height)
  )

  /** Whether the pointer button was clicked within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasClickedWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasClickedWithin(Rectangle(x, y, width, height), Some(button))

  /** Whether the pointer was clicked within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasClickedWithin(bounds: Rectangle, button: Option[MouseButton]): Boolean =
    pointers.clickedPositionsWith(button, pointerType).exists(bounds.isPointWithin)

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasWithin(bounds: Rectangle): Boolean = pointers.wasPointerPositionWithin(bounds, pointerType)

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasWithin(Rectangle(x, y, width, height))
