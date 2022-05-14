package indigoextras.geometry

import indigo.shared.collections.NonEmptyList
import indigo.shared.temporal.Signal
import indigo.shared.time.Seconds

import scala.annotation.tailrec

opaque type Bezier = List[Vertex]
object Bezier:

  inline def apply(vertices: List[Vertex]): Bezier = vertices

  inline def apply(start: Vertex, vertices: Vertex*): Bezier =
    start :: vertices.toList

  extension (b: Bezier)
    /** Calculate the position of a Bezier curve using specialised calculations
      * for linear, quadratic and cubic curves.
      */
    def at(unitInterval: Double): Vertex =
      b match
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
          reduce(b, Math.max(0, Math.min(1, unitInterval)))

    def toVertices(subdivisions: Int): List[Vertex] =
      (0 to subdivisions).toList.map { i =>
        b.at((1 / subdivisions.toDouble) * i.toDouble)
      }

    def toPolygon(subdivisions: Int): Polygon =
      Polygon.Open(toVertices(subdivisions))

    def toLineSegments(subdivisions: Int): List[LineSegment] =
      Polygon.Open(toVertices(subdivisions)).lineSegments

    def toSignal(duration: Seconds): Signal[Vertex] =
      Signal { t =>
        b.at(t.toDouble / duration.toDouble)
      }

    def bounds: BoundingBox =
      BoundingBox.fromVertices(b)

  def pure(start: Vertex, vertices: List[Vertex]): Bezier =
    Bezier(start :: vertices.toList)

  def fromPoints(vertices: List[Vertex]): Option[Bezier] =
    NonEmptyList.fromList(vertices).map(fromVerticesNel)

  def fromVerticesNel(vertices: NonEmptyList[Vertex]): Bezier =
    Bezier(vertices.toList)

  /** Calculate the position of a Bezier curve using the same calculation
    * method regardless of vertex count.
    */
  def atUnspecialised(bezier: Bezier, unitInterval: Double): Vertex =
    reduce(bezier, Math.max(0, Math.min(1, unitInterval)))

  def interpolate(a: Vertex, b: Vertex, unitInterval: Double): Vertex =
    a + ((b - a) * unitInterval)

  @tailrec
  def reduce(vertices: List[Vertex], unitInterval: Double): Vertex =
    @tailrec
    def pair(remaining: List[Vertex], acc: List[(Vertex, Vertex)]): List[(Vertex, Vertex)] =
      remaining match
        case Nil =>
          acc.reverse

        case _ :: Nil =>
          acc.reverse

        case x :: y :: xs =>
          pair(y :: xs, (x, y) :: acc)

    vertices match
      case Nil =>
        Vertex.zero

      case x :: Nil =>
        x

      case p1 :: p2 :: Nil =>
        interpolate(p1, p2, unitInterval)

      case ps =>
        reduce(pair(ps, Nil).map(p => interpolate(p._1, p._2, unitInterval)), unitInterval)

object BezierMath:
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
