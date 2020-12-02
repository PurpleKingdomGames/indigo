package indigoextras.geometry

import scala.annotation.tailrec
import indigo.shared.EqualTo
import indigo.shared.temporal.Signal
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Seconds
import indigo.shared.EqualTo._

final class Bezier(val vertices: List[Vertex]) extends AnyVal {

  def at(unitInterval: Double): Vertex =
    Bezier.at(this, unitInterval)

  def toVertices(subdivisions: Int): List[Vertex] =
    Bezier.toVertices(this, subdivisions)

  def toPolygon(subdivisions: Int): Polygon =
    Bezier.toPolygon(this, subdivisions)

  def toLineSegments(subdivisions: Int): List[LineSegment] =
    Bezier.toLineSegments(this, subdivisions)

  def toSignal(duration: Seconds): Signal[Vertex] =
    Bezier.toSignal(this, duration)

  def bounds: BoundingBox =
    BoundingBox.fromVertices(vertices)

  def ===(other: Bezier): Boolean =
    implicitly[EqualTo[Bezier]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  override def toString: String =
    s"Bezier(vertices = ${vertices.map(_.toString()).mkString("[", ",", "]")})"

}

object Bezier {

  implicit val bEqualTo: EqualTo[Bezier] = {
    val eqPt = implicitly[EqualTo[Vertex]]

    EqualTo.create { (a, b) =>
      a.vertices.length === b.vertices.length &&
      a.vertices.zip(b.vertices).forall(p => eqPt.equal(p._1, p._2))
    }
  }

  def apply(start: Vertex, vertices: Vertex*): Bezier =
    new Bezier(start :: vertices.toList)

  def pure(start: Vertex, vertices: List[Vertex]): Bezier =
    new Bezier(start :: vertices.toList)

  def fromPoints(vertices: List[Vertex]): Option[Bezier] =
    NonEmptyList.fromList(vertices).map(fromVerticesNel)

  def fromVerticesNel(vertices: NonEmptyList[Vertex]): Bezier =
    new Bezier(vertices.toList)

  /**
    * Calculate the position of a Bezier curve using specialised calculations
    * for linear, quadratic and cubic curves.
    */
  def at(bezier: Bezier, unitInterval: Double): Vertex =
    bezier.vertices match {
      case Nil =>
        Vertex.zero

      case x :: Nil =>
        x

      case p0 :: p1 :: Nil =>
        BezierMath.linearNormalised(unitInterval, p0, p1)

      case p0 :: p1 :: p2 :: Nil =>
        BezierMath.quadraticNormalised(unitInterval, p0, p1, p2)

      case p0 :: p1 :: p2 :: p3 :: Nil =>
        BezierMath.cubicNormalised(unitInterval, p0, p1, p2, p3)

      case _ =>
        reduce(bezier.vertices, Math.max(0, Math.min(1, unitInterval)))
    }

  /**
    * Calculate the position of a Bezier curve using the same calculation
    * method regardless of vertex count.
    */
  def atUnspecialised(bezier: Bezier, unitInterval: Double): Vertex =
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

  def toSignal(bezier: Bezier, duration: Seconds): Signal[Vertex] =
    Signal { t =>
      bezier.at(t.toDouble / duration.toDouble)
    }
}

object BezierMath {

  /*
   * Linear
   * B(t) = P0 + ((P1 - P0) * t)
   *      = ((1 - t) * P0) +
   *        (t * P1)
   *
   *      = (1 - t) * P0 + t * P1
   */
  def linear(t: Double, p0: Double, p1: Double): Double =
    (1 - t) * p0 + t * p1

  def linearWithVertices(t: Double, p0: Vertex, p1: Vertex): Vertex =
    Vertex(linear(t, p0.x, p1.x), linear(t, p0.y, p1.y))

  def linearNormalised(t: Double, p0: Vertex, p1: Vertex): Vertex =
    linearWithVertices(Math.max(0, Math.min(1, t)), p0, p1)

  /*
   * Quadratic
   * B(t) = (((1 - t) * (1 - t)) * P0) +
   *        (((2 * t) * (1 - t) * P1) +
   *        ((t * t) * P2)
   *
   *        pow(1 - t, 2) * P0 + 2 * t * (1 - t) * P1 + pow(t, 2) * P2
   */
  def quadratic(t: Double, p0: Double, p1: Double, p2: Double): Double =
    Math.pow(1 - t, 2) * p0 + 2 * t * (1 - t) * p1 + Math.pow(t, 2) * p2

  def quadraticWithVertices(t: Double, p0: Vertex, p1: Vertex, p2: Vertex): Vertex =
    Vertex(quadratic(t, p0.x, p1.x, p2.x), quadratic(t, p0.y, p1.y, p2.y))

  def quadraticNormalised(t: Double, p0: Vertex, p1: Vertex, p2: Vertex): Vertex =
    quadraticWithVertices(Math.max(0, Math.min(1, t)), p0, p1, p2)

  /*
   * Cubic
   * B(t) = (((1 - t) * (1 - t) * (1 - t)) * P0) +
   *        (((3 * t) * ((1 - t) * (1 - t)) * P1) +
   *        ((3 * (t * t)) * (1 -t)) * P2) +
   *        ((t * t * t) * P3)
   *
   *      = pow(1 - t, 3) * P0 + 3 * t * pow(1 - t, 2) * P1 + 3 * pow(t, 2) * (1 - t) * P2 + pow(t, 3) * P3
   */
  def cubic(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double =
    Math.pow(1 - t, 3) * p0 + 3 * t * Math.pow(1 - t, 2) * p1 + 3 * Math.pow(t, 2) * (1 - t) * p2 + Math.pow(t, 3) * p3

  def cubicWithVertices(t: Double, p0: Vertex, p1: Vertex, p2: Vertex, p3: Vertex): Vertex =
    Vertex(cubic(t, p0.x, p1.x, p2.x, p3.x), cubic(t, p0.y, p1.y, p2.y, p3.y))

  def cubicNormalised(t: Double, p0: Vertex, p1: Vertex, p2: Vertex, p3: Vertex): Vertex =
    cubicWithVertices(Math.max(0, Math.min(1, t)), p0, p1, p2, p3)
}
