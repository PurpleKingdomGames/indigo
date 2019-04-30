package indigoexts.geometry

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.AsString
import indigo.shared.EqualTo
import scala.annotation.tailrec

sealed trait Polygon {
  val vertices: List[Point]

  def bounds: Rectangle =
    Rectangle.fromPointCloud(vertices)

  def edgeCount: Int =
    this match {
      case Polygon.Open(vs) =>
        vs.length - 1

      case Polygon.Closed(vs) =>
        vs.length
    }

  def lineSegments: List[LineSegment] =
    Polygon.toLineSegments(this)

  //TODO: Propatage line segment-type functions to lines.

  def addVertex(vertex: Point): Polygon =
    this match {
      case Polygon.Open(vs) =>
        Polygon.Open(vs :+ vertex)

      case Polygon.Closed(vs) =>
        Polygon.Closed(vs :+ vertex)
    }

  def contains(point: Point): Boolean =
    this match {
      case Polygon.Open(_) =>
        false

      case Polygon.Closed(_) =>
      println(point.asString)
        ???
      // bounds.isPointWithin(point) //&&  for all line segments is point on wrong side of normal?
    }

  def asString: String =
    implicitly[AsString[Polygon]].show(this)

  def ===(other: Polygon): Boolean =
    implicitly[EqualTo[Polygon]].equal(this, other)

}

object Polygon {

  def fromRectangle(rectangle: Rectangle): Closed =
    Closed(rectangle.topLeft, rectangle.bottomLeft, rectangle.bottomRight, rectangle.topRight)

  def toLineSegments(polygon: Polygon): List[LineSegment] = {
    @tailrec
    def rec(remaining: List[Point], current: Point, acc: List[LineSegment]): List[LineSegment] =
      remaining match {
        case Nil =>
          acc

        case x :: xs =>
          rec(xs, x, LineSegment(current, x) :: acc)
      }

    polygon match {
      case Open(Nil) =>
        Nil

      case Closed(Nil) =>
        Nil

      case Open(h :: t) =>
        rec(t, h, Nil)

      case Closed(h :: t) =>
        rec(t :+ h, h, Nil)
    }
  }

  implicit val polygonAsString: AsString[Polygon] = {
    val s = implicitly[AsString[List[Point]]]

    AsString.create {
      case Open(vs) =>
        s"Polygon.Open(${s.show(vs)})"

      case Closed(vs) =>
        s"Polygon.Closed(${s.show(vs)})"
    }
  }

  implicit val polygonEqualTo: EqualTo[Polygon] = {
    val e = implicitly[EqualTo[List[Point]]]

    EqualTo.create {
      case (Open(vsA), Open(vsB)) =>
        e.equal(vsA, vsB)

      case (Closed(vsA), Closed(vsB)) =>
        e.equal(vsA, vsB)

      case (_, _) =>
        false
    }
  }

  final class Open(val vertices: List[Point]) extends Polygon
  object Open {

    implicit val openEqualTo: EqualTo[Closed] =
      EqualTo.create { (a, b) =>
        polygonEqualTo.equal(a, b)
      }

    val empty: Open =
      Open(Nil)

    def apply(vertices: List[Point]): Open =
      new Open(vertices)

    def apply(vertices: Point*): Open =
      new Open(vertices.toList)

    def unapply(polygon: Open): Option[List[Point]] =
      Option(polygon.vertices)
  }

  final class Closed(val vertices: List[Point]) extends Polygon
  object Closed {

    implicit val closedEqualTo: EqualTo[Closed] =
      EqualTo.create { (a, b) =>
        polygonEqualTo.equal(a, b)
      }

    val empty: Closed =
      Closed(Nil)

    def apply(vertices: List[Point]): Closed =
      new Closed(vertices)

    def apply(vertices: Point*): Closed =
      new Closed(vertices.toList)

    def unapply(polygon: Closed): Option[List[Point]] =
      Option(polygon.vertices)
  }

}
