package indigo.shared.input

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle

trait PositionalInputState:
  /** Coordinates relative to the magnification level, regardless of whether the pointer has a verified position
    */
  def position: Point = maybePosition.getOrElse(Point.zero)

  /** Coordinates relative to the magnification level, if the pointer is currently has a position
    */
  val maybePosition: Option[Point]

  /** The X position relative to the magnification level
    */
  def x: Int = position.x

  /** The Y position relative to the magnification level
    */
  def y: Int = position.y

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param bounds
    *   The bounds to check
    * @return
    */
  def wasWithin(bounds: Rectangle): Boolean = bounds.isPointWithin(position)

  /** Whether the pointer position was within the specified bounds in this frame
    *
    * @param x
    *   The x coordinate of the bounds to check
    * @param y
    *   The y coordinate of the bounds to check
    * @param width
    *   The width of the bounds to check
    * @param height
    *   The height of the bounds to check
    * @return
    */
  def wasWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasWithin(Rectangle(x, y, width, height))

  /** Whether the pointer was at the specified position in this frame
    *
    * @param position
    *   The position to check
    * @return
    */
  def wasAt(position: Point): Boolean = this.position == position

  /** Whether the pointer was at the specified position in this frame
    *
    * @param x
    *   The x coordinate of the position to check
    * @param y
    *   The y coordinate of the position to check
    * @return
    */
  def wasAt(x: Int, y: Int): Boolean =
    wasAt(Point(x, y))

  @deprecated("Use `wasAt` instead", "0.22.0")
  def wasPositionAt(position: Point): Boolean = wasAt(position)

  @deprecated("Use `wasAt` instead", "0.22.0")
  def wasPositionAt(x: Int, y: Int): Boolean = wasAt(Point(x, y))

  @deprecated("Use `wasWithin` instead", "0.22.0")
  def wasPositionWithin(bounds: Rectangle): Boolean = wasWithin(bounds)

  @deprecated("Use `wasWithin` instead", "0.22.0")
  def wasPositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasWithin(Rectangle(x, y, width, height))
