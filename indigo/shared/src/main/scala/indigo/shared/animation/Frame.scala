package indigo.shared.animation

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.{EqualTo, AsString}
import indigo.shared.AsString._
import indigo.shared.time.Millis
import indigo.shared.datatypes.Material

final class Frame(val crop: Rectangle, val duration: Millis, val frameMaterial: Option[Material])

object Frame {

  implicit val frameEqualTo: EqualTo[Frame] =
    EqualTo.create { (a, b) =>
      a.crop === b.crop && a.duration === b.duration
    }

  implicit val frameAsString: AsString[Frame] =
    AsString.create(f => s"Frame(${f.crop.show}, ${f.duration.show})")

  def apply(crop: Rectangle, duration: Millis): Frame =
    new Frame(crop, duration, None)

  def apply(crop: Rectangle, duration: Millis, frameMaterial: Material): Frame =
    new Frame(crop, duration, Some(frameMaterial))

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), Millis(1))

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Millis): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}
