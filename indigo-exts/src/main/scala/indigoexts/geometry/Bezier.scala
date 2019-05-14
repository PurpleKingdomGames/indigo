package indigoexts.geometry

import indigo.shared.datatypes.Point
import scala.annotation.tailrec
import indigo.shared.temporal.Signal

final class Bezier(val points: List[Point]) {

  def at(unitInterval: Double): Point =
    Bezier.at(this, unitInterval)

  def toLineSegments(subdivisions: Int): List[LineSegment] =
    Bezier.toLineSegments(this, subdivisions)

  def toSignal: Signal[Point] =
    Bezier.toSignal(this)

}

object Bezier {

  def apply(start: Point, points: Point*): Bezier =
    new Bezier(start :: points.toList)

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

  def toLineSegments(bezier: Bezier, subdivisions: Int): List[LineSegment] = {
    println(bezier)
    println(subdivisions.toString)
    Nil
  }

  def toSignal(bezier: Bezier): Signal[Point] = {
    println(bezier)
    Signal.fixed(Point.zero)
  }
}
