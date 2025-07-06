package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.MouseButton

trait ButtonInputState:
  val buttons: Batch[MouseButton]
  val clicks: Batch[(MouseButton, Point)]
  val downButtons: Batch[(MouseButton, Point)]
  val upButtons: Batch[(MouseButton, Point)]

  /** Whether the specified button is down on any pointer
    *
    * @param button
    *   The button to check
    * @return
    */
  def isButtonDown(button: MouseButton): Boolean = buttons.exists(_ == button)

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

  /** The positions that the pointer was clicked at in this frame
    */
  lazy val isClickedAt: Batch[Point] = clicks.map(_._2)

  /** The positions that the pointer was up at in this frame
    */
  lazy val isButtonUpAt: Batch[Point] = upButtons.map(_._2)

  /** The positions that the pointer was down at in this frame
    */
  lazy val isButtonDownAt: Batch[Point] = downButtons.map(_._2)

  /** Whether the pointer was clicked this frame
    */
  lazy val isClicked: Boolean = clicks.isEmpty == false

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
  def pressed(button: MouseButton): Boolean = downButtons.exists(_._1 == button)

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
  def released(button: MouseButton): Boolean = upButtons.exists(_._1 == button)

  /** The first position where the specified button was up on this frame
    *
    * @param button
    * @return
    */
  def maybeUpAtPositionWith(button: MouseButton): Option[Point] =
    upButtons.filter(_._1 == button).headOption.map(_._2)

  /** The first position where the specified button was down on this frame
    *
    * @param button
    * @return
    */
  def maybeDownAtPositionWith(button: MouseButton): Option[Point] =
    downButtons.filter(_._1 == button).headOption.map(_._2)

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

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param position
    * @param button
    */
  def wasButtonUpAt(position: Point, button: MouseButton): Boolean =
    upButtons.exists(_ == (button, position))

  /** Whether the specified button was up at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasButtonUpAt(x: Int, y: Int, button: MouseButton): Boolean = wasButtonUpAt(Point(x, y), button)

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param position
    * @param button
    * @return
    */
  def wasButtonDownAt(position: Point, button: MouseButton): Boolean =
    downButtons.exists(_ == (button, position))

  /** Whether the specified button was down at the specified position in this frame
    *
    * @param x
    * @param y
    * @param button
    * @return
    */
  def wasButtonDownAt(x: Int, y: Int, button: MouseButton): Boolean = wasButtonDownAt(Point(x, y), button)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasButtonUpWithin(bounds: Rectangle): Boolean =
    upButtons.exists { case (b, position) => bounds.isPointWithin(position) }

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasButtonUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasButtonUpWithin(
    Rectangle(x, y, width, height)
  )

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasButtonUpWithin(bounds: Rectangle, button: MouseButton): Boolean =
    upButtons.exists { case (b, position) =>
      b == button && bounds.isPointWithin(position)
    }

  /** Whether the pointer was up within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasButtonUpWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasButtonUpWithin(Rectangle(x, y, width, height), button)

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @return
    */
  def wasButtonDownWithin(bounds: Rectangle): Boolean =
    downButtons.exists { case (b, position) => bounds.isPointWithin(position) }

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @return
    */
  def wasButtonDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasButtonDownWithin(
    Rectangle(x, y, width, height)
  )

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param bounds
    * @param button
    * @return
    */
  def wasButtonDownWithin(bounds: Rectangle, button: MouseButton): Boolean =
    downButtons.exists { case (b, position) =>
      b == button && bounds.isPointWithin(position)
    }

  /** Whether the pointer was down within the specified bounds in this frame
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param button
    * @return
    */
  def wasButtonDownWithin(x: Int, y: Int, width: Int, height: Int, button: MouseButton): Boolean =
    wasButtonDownWithin(Rectangle(x, y, width, height), button)

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
    clicks.exists { case (b, position) => (button == None || Some(b) == button) && bounds.isPointWithin(position) }
