package indigo.shared.input

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.PointerId

trait PositionalInputState:
  /** Unique pointer identifier
    */
  val pointerId: PointerId

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
    * @return
    */
  def wasWithin(bounds: Rectangle): Boolean =
    bounds.isPointWithin(position)

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

  def wasPositionAt(position: Point): Boolean =
    this.position == position

  def wasPositionAt(x: Int, y: Int): Boolean =
    wasPositionAt(Point(x, y))

  def wasPositionWithin(bounds: Rectangle): Boolean =
    bounds.isPointWithin(position)

  def wasPositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasPositionWithin(Rectangle(x, y, width, height))
