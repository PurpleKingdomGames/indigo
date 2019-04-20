package indigo.shared.animation

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.{EqualTo, AsString}
import indigo.shared.EqualTo._
import indigo.shared.AsString._

final class Frame(val bounds: Rectangle, val duration: Int)

object Frame {

  implicit val frameEqualTo: EqualTo[Frame] =
    EqualTo.create { (a, b) =>
      a.bounds === b.bounds && a.duration === b.duration
    }

  implicit val frameAsString: AsString[Frame] =
    AsString.create(f => s"Frame(${f.bounds.show}, ${f.duration.show})")

  def apply(bounds: Rectangle, duration: Int): Frame =
    new Frame(bounds, duration)

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), 1)

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}
