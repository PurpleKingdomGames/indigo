package indigoexts.geometry

import indigo.shared.datatypes.Point
import scala.annotation.tailrec
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis
import indigo.shared.datatypes.Rectangle
import indigo.shared.collections.NonEmptyList

final class Bezier(private val points: List[Point]) {

  def at(unitInterval: Double): Point =
    Bezier.at(this, unitInterval)

  def toPoints(subdivisions: Int): List[Point] =
    Bezier.toPoints(this, subdivisions)

  def toPolygon(subdivisions: Int): Polygon =
    Bezier.toPolygon(this, subdivisions)

  def toLineSegments(subdivisions: Int): List[LineSegment] =
    Bezier.toLineSegments(this, subdivisions)

  def toSignal(duration: Millis): Signal[Point] =
    Bezier.toSignal(this, duration)

  def bounds: Rectangle =
    Rectangle.fromPointCloud(points)

}

object Bezier {

  def apply(start: Point, points: Point*): Bezier =
    new Bezier(start :: points.toList)

  def pure(start: Point, points: List[Point]): Bezier =
    new Bezier(start :: points.toList)

  def fromPoints(points: List[Point]): Option[Bezier] =
    NonEmptyList.fromList(points).map(fromPointsNel)

  def fromPointsNel(points: NonEmptyList[Point]): Bezier =
    new Bezier(points.toList)

  def at(bezier: Bezier, unitInterval: Double): Point =
    reduce(bezier.points, Math.max(0, Math.min(1, unitInterval)))

  def interpolate(a: Point, b: Point, unitInterval: Double): Point =
    a + ((b - a).toVector * unitInterval).toPoint

  @tailrec
  def reduce(points: List[Point], unitInterval: Double): Point = {
    @tailrec
    def pair(remaining: List[Point], acc: List[(Point, Point)]): List[(Point, Point)] =
      remaining match {
        case Nil =>
          acc.reverse

        case _ :: Nil =>
          acc.reverse

        case x :: y :: xs =>
          pair(y :: xs, (x, y) :: acc)
      }

    points match {
      case Nil =>
        Point.zero

      case x :: Nil =>
        x

      case p1 :: p2 :: Nil =>
        interpolate(p1, p2, unitInterval)

      case ps =>
        reduce(pair(ps, Nil).map(p => interpolate(p._1, p._2, unitInterval)), unitInterval)
    }
  }

  def toPoints(bezier: Bezier, subdivisions: Int): List[Point] =
    (0 to subdivisions).toList.map { i =>
      bezier.at((1 / subdivisions.toDouble) * i.toDouble)
    }

  def toPolygon(bezier: Bezier, subdivisions: Int): Polygon =
    Polygon.Open(toPoints(bezier, subdivisions))

  def toLineSegments(bezier: Bezier, subdivisions: Int): List[LineSegment] =
    Polygon.Open(toPoints(bezier, subdivisions)).lineSegments

  def toSignal(bezier: Bezier, duration: Millis): Signal[Point] =
    Signal { t =>
      bezier.at(t.toDouble / duration.toDouble)
    }
}
