package indigo.gameengine.scenegraph.animation

import indigo.Rectangle
import indigo.Point
import indigo.{EqualTo, AsString}
import indigo.EqualTo._
import indigo.AsString._

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
