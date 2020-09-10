package indigo.shared.animation

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.EqualTo
import indigo.shared.time.Millis
import indigo.shared.datatypes.Material

final case class Frame(crop: Rectangle, duration: Millis, frameMaterial: Option[Material])

object Frame {

  implicit val frameEqualTo: EqualTo[Frame] =
    EqualTo.create { (a, b) =>
      a.crop === b.crop && a.duration === b.duration
    }

  def apply(crop: Rectangle, duration: Millis): Frame =
    Frame(crop, duration, None)

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), Millis(1))

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Millis): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}
