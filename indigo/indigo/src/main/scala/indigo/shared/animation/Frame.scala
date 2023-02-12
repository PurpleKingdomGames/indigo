package indigo.shared.animation

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.time.Millis

final case class Frame(crop: Rectangle, duration: Millis) derives CanEqual:
  def position: Point = crop.position
  def size: Size      = crop.size

object Frame:

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Size(width, height)), Millis(1))

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Millis): Frame =
    Frame(Rectangle(Point(x, y), Size(width, height)), duration)
