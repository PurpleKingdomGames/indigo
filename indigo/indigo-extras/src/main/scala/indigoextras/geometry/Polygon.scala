package indigoextras.geometry

import indigo.shared.datatypes.Rectangle
import indigo.shared.EqualTo
import scala.annotation.tailrec

sealed trait Polygon {
  val vertices: List[Vertex]

  def bounds: BoundingBox =
    BoundingBox.fromVertices(vertices)

  def edgeCount: Int =
    this match {
      case Polygon.Open(vs) =>
        vs.length - 1

      case Polygon.Closed(vs) =>
        vs.length
    }

  lazy val lineSegments: List[LineSegment] =
    Polygon.toLineSegments(this)

  //TODO: Propatage line segment-type functions to lines.

  def addVertex(vertex: Vertex): Polygon =
    this match {
      case Polygon.Open(vs) =>
        Polygon.Open(vs ++ List(vertex))

      case Polygon.Closed(vs) =>
        Polygon.Closed(vs ++ List(vertex))
    }

  def contains(vertex: Vertex): Boolean =
    this match {
      case Polygon.Open(_) =>
        false

      case p @ Polygon.Closed(_) =>
        bounds.isVertexWithin(vertex) && p.lineSegments.forall(l => !l.isFacingVertex(vertex))
    }

  def lineIntersectCheck(lineSegment: LineSegment): Boolean =
    lineSegments.exists(_.intersectWithLine(lineSegment))

  def rectangleIntersectCheck(rectangle: Rectangle): Boolean =
    Polygon.fromRectangle(rectangle).lineSegments.exists(lineIntersectCheck)

  def polygonIntersectCheck(polygon: Polygon): Boolean =
    polygon.lineSegments.exists(lineIntersectCheck)

  def ===(other: Polygon): Boolean =
    implicitly[EqualTo[Polygon]].equal(this, other)

  override def toString: String =
    this match {
      case Polygon.Open(vs) =>
        s"Polygon.Open(${vs.toString()})"

      case Polygon.Closed(vs) =>
        s"Polygon.Closed(${vs.toString()})"
    }

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[Polygon])
      this === obj.asInstanceOf[Polygon]
    else false
}

object Polygon {

  def fromRectangle(rectangle: Rectangle): Closed =
    Closed(
      Vertex.fromPoint(rectangle.topLeft),
      Vertex.fromPoint(rectangle.bottomLeft),
      Vertex.fromPoint(rectangle.bottomRight),
      Vertex.fromPoint(rectangle.topRight)
    )

  def fromBoundingBox(boundingBox: BoundingBox): Closed =
    Closed(
      boundingBox.topLeft,
      boundingBox.bottomLeft,
      boundingBox.bottomRight,
      boundingBox.topRight
    )

  def toLineSegments(polygon: Polygon): List[LineSegment] = {
    @tailrec
    def rec(remaining: List[Vertex], current: Vertex, acc: List[LineSegment]): List[LineSegment] =
      remaining match {
        case Nil =>
          acc.reverse

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
        rec(t ++ List(h), h, Nil)
    }
  }

  implicit val polygonEqualTo: EqualTo[Polygon] = {
    val e = implicitly[EqualTo[List[Vertex]]]

    EqualTo.create {
      case (Open(vsA), Open(vsB)) =>
        e.equal(vsA, vsB)

      case (Closed(vsA), Closed(vsB)) =>
        e.equal(vsA, vsB)

      case (_, _) =>
        false
    }
  }

  final case class Open(vertices: List[Vertex]) extends Polygon
  object Open {

    implicit val openEqualTo: EqualTo[Closed] =
      EqualTo.create { (a, b) =>
        polygonEqualTo.equal(a, b)
      }

    val empty: Open =
      Open(Nil)

    def apply(vertices: Vertex*): Open =
      new Open(vertices.toList)
  }

  final case class Closed(vertices: List[Vertex]) extends Polygon
  object Closed {

    implicit val closedEqualTo: EqualTo[Closed] =
      EqualTo.create { (a, b) =>
        polygonEqualTo.equal(a, b)
      }

    val empty: Closed =
      Closed(Nil)

    def apply(vertices: Vertex*): Closed =
      new Closed(vertices.toList)
  }

}
