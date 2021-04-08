package indigo.shared.animation

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

import indigo.shared.time.Millis

final case class Frame(crop: Rectangle, duration: Millis)

object Frame {

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), Millis(1))

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Millis): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}
