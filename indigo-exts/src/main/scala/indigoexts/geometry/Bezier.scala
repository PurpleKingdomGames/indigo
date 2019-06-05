package indigoexts.geometry

import scala.annotation.tailrec
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis
import indigo.shared.collections.NonEmptyList

final class Bezier(private val vertices: List[Vertex]) {

  def at(unitInterval: Double): Vertex =
    Bezier.at(this, unitInterval)

  def toVertices(subdivisions: Int): List[Vertex] =
    Bezier.toVertices(this, subdivisions)

  def toPolygon(subdivisions: Int): Polygon =
    Bezier.toPolygon(this, subdivisions)

  def toLineSegments(subdivisions: Int): List[LineSegment] =
    Bezier.toLineSegments(this, subdivisions)

  def toSignal(duration: Millis): Signal[Vertex] =
    Bezier.toSignal(this, duration)

  def bounds: BoundingBox =
    BoundingBox.fromVertices(vertices)

}

object Bezier {

  def apply(start: Vertex, vertices: Vertex*): Bezier =
    new Bezier(start :: vertices.toList)

  def pure(start: Vertex, vertices: List[Vertex]): Bezier =
    new Bezier(start :: vertices.toList)

  def fromPoints(vertices: List[Vertex]): Option[Bezier] =
    NonEmptyList.fromList(vertices).map(fromVerticesNel)

  def fromVerticesNel(vertices: NonEmptyList[Vertex]): Bezier =
    new Bezier(vertices.toList)

  def at(bezier: Bezier, unitInterval: Double): Vertex =
    reduce(bezier.vertices, Math.max(0, Math.min(1, unitInterval)))

  def interpolate(a: Vertex, b: Vertex, unitInterval: Double): Vertex =
    a + ((b - a) * unitInterval)

  @tailrec
  def reduce(vertices: List[Vertex], unitInterval: Double): Vertex = {
    @tailrec
    def pair(remaining: List[Vertex], acc: List[(Vertex, Vertex)]): List[(Vertex, Vertex)] =
      remaining match {
        case Nil =>
          acc.reverse

        case _ :: Nil =>
          acc.reverse

        case x :: y :: xs =>
          pair(y :: xs, (x, y) :: acc)
      }

    vertices match {
      case Nil =>
        Vertex.zero

      case x :: Nil =>
        x

      case p1 :: p2 :: Nil =>
        interpolate(p1, p2, unitInterval)

      case ps =>
        reduce(pair(ps, Nil).map(p => interpolate(p._1, p._2, unitInterval)), unitInterval)
    }
  }

  def toVertices(bezier: Bezier, subdivisions: Int): List[Vertex] =
    (0 to subdivisions).toList.map { i =>
      bezier.at((1 / subdivisions.toDouble) * i.toDouble)
    }

  def toPolygon(bezier: Bezier, subdivisions: Int): Polygon =
    Polygon.Open(toVertices(bezier, subdivisions))

  def toLineSegments(bezier: Bezier, subdivisions: Int): List[LineSegment] =
    Polygon.Open(toVertices(bezier, subdivisions)).lineSegments

  def toSignal(bezier: Bezier, duration: Millis): Signal[Vertex] =
    Signal { t =>
      bezier.at(t.toDouble / duration.toDouble)
    }
}
