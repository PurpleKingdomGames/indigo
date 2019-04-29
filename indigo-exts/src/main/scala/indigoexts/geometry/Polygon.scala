package indigoexts.geometry

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle

sealed trait Polygon {
  val vertices: List[Point]

  def bounds: Rectangle =
    Rectangle.fromPointCloud(vertices)

  //TODO: Order is important, verts and indices? Or assume ordered?
  //TODO: As line segments
  //TODO: Propatage line segment-type functions to lines.
  // Edge count

  def addVertex(vertex: Point): Polygon =
    this match {
      case Polygon.Open(vs) =>
        Polygon.Open(vs :+ vertex)

      case Polygon.Closed(vs) =>
        Polygon.Closed(vs :+ vertex)
    }

}

object Polygon {

  final class Open(val vertices: List[Point]) extends Polygon
  object Open {
    val empty: Open =
      Open(Nil)

    def apply(vertices: List[Point]): Open =
      new Open(vertices)

    def unapply(polygon: Open): Option[List[Point]] =
      Option(polygon.vertices)
  }

  final class Closed(val vertices: List[Point]) extends Polygon
  object Closed {
    val empty: Closed =
      Closed(Nil)

    def apply(vertices: List[Point]): Closed =
      new Closed(vertices)

    def unapply(polygon: Closed): Option[List[Point]] =
      Option(polygon.vertices)
  }

}
