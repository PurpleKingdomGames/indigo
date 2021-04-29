package indigoextras.geometry

import indigo.shared.datatypes.Rectangle

import scala.annotation.tailrec

sealed trait Polygon derives CanEqual {
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
        bounds.contains(vertex) && p.lineSegments.forall(l => !l.isFacingVertex(vertex))
    }

  def lineIntersectCheck(lineSegment: LineSegment): Boolean =
    lineSegments.exists(_.intersectsWithLine(lineSegment))

  def rectangleIntersectCheck(rectangle: Rectangle): Boolean =
    Polygon.fromRectangle(rectangle).lineSegments.exists(lineIntersectCheck)

  def polygonIntersectCheck(polygon: Polygon): Boolean =
    polygon.lineSegments.exists(lineIntersectCheck)

  override def toString: String =
    this match {
      case Polygon.Open(vs) =>
        s"Polygon.Open(${vs.toString()})"

      case Polygon.Closed(vs) =>
        s"Polygon.Closed(${vs.toString()})"
    }

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

  final case class Open(vertices: List[Vertex]) extends Polygon
  object Open {

    val empty: Open =
      Open(Nil)

    def apply(vertices: Vertex*): Open =
      new Open(vertices.toList)
  }

  final case class Closed(vertices: List[Vertex]) extends Polygon
  object Closed {

    val empty: Closed =
      Closed(Nil)

    def apply(vertices: Vertex*): Closed =
      new Closed(vertices.toList)
  }

}
