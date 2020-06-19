package indigoextras.geometry

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point

object PolygonTests extends TestSuite {

  // Polygons
  val open: Polygon.Open =
    Polygon.Open(
      Vertex(0, 0),
      Vertex(5, 5),
      Vertex(10, 0),
      Vertex(5, -5)
    )

  val closed: Polygon.Closed =
    Polygon.Closed(
      Vertex(0, 0),
      Vertex(5, 5),
      Vertex(10, 0),
      Vertex(5, -5)
    )

  // Lines
  val intersectingLine: LineSegment =
    LineSegment(Vertex(0, 5), Vertex(5, 0))

  val noneIntersectingLine: LineSegment =
    LineSegment(Vertex(0, 5), Vertex(0, 4))

  val intersectingLineWithClosed: LineSegment =
    LineSegment(Vertex(0, -5), Vertex(5, 0))

  // Rectangles
  val intersectingRectangle: Rectangle =
    Rectangle(Point(0, 5), Point(5, 0))

  val noneIntersectingRectangle: Rectangle =
    Rectangle(Point(0, 5), Point(1, 1))

  val intersectingRectangleWithClosed: Rectangle =
    Rectangle(Point(0, -5), Point(4, 4))

  var tests: Tests =
    Tests {

      "Construction" - {
        "should be able to create an open polygon" - {
          open.edgeCount ==> 3
        }

        "should be able to create a closed polygon" - {
          closed.edgeCount ==> 4
        }

        "should be able to add a vertex" - {
          Polygon.Open.empty.addVertex(Vertex.zero).vertices.length ==> 1
        }

        "should be able to produce a bounding rectangle" - {
          closed.bounds.toRectangle === Rectangle(0, -5, 10, 10) ==> true
        }

        "should be able to create a polygon from a rectangle" - {
          val expected: Polygon =
            Polygon.Closed(
              Vertex(0, 0),
              Vertex(0, 10),
              Vertex(10, 10),
              Vertex(10, 0)
            )

          val actual: Polygon =
            Polygon.fromRectangle(Rectangle(Point(0, 0), Point(10, 10)))

          expected === actual ==> true
        }
      }

      "Operations" - {

        "should be able to produce line segments (open)" - {

          val expected: List[LineSegment] =
            List(
              LineSegment(Vertex(0, 0), Vertex(5, 5)),
              LineSegment(Vertex(5, 5), Vertex(10, 0)),
              LineSegment(Vertex(10, 0), Vertex(5, -5))
            )

          val actual: List[LineSegment] =
            open.lineSegments

          actual === expected ==> true
        }

        "should be able to produce line segments (closed)" - {

          val expected: List[LineSegment] =
            List(
              LineSegment(Vertex(0, 0), Vertex(5, 5)),
              LineSegment(Vertex(5, 5), Vertex(10, 0)),
              LineSegment(Vertex(10, 0), Vertex(5, -5)),
              LineSegment(Vertex(5, -5), Vertex(0, 0))
            )

          val actual: List[LineSegment] =
            closed.lineSegments

          actual === expected ==> true
        }

      }

      "Intersections" - {
        "contains point (open shapes can't contain)" - {
          open.contains(Vertex(2, 1)) ==> false
        }

        "contains point (closed)" - {
          "totally enclosed" - {
            closed.contains(Vertex(2, 0)) ==> true
          }

          "within bounds but not inside polygon" - {
            closed.contains(Vertex(1, 4)) ==> false
          }
        }

        "intersects with line (open)" - {
          open.lineIntersectCheck(intersectingLine) ==> true
          open.lineIntersectCheck(noneIntersectingLine) ==> false
          open.lineIntersectCheck(intersectingLineWithClosed) ==> false
        }

        "intersects with line (closed)" - {
          closed.lineIntersectCheck(intersectingLine) ==> true
          closed.lineIntersectCheck(noneIntersectingLine) ==> false
          closed.lineIntersectCheck(intersectingLineWithClosed) ==> true
        }

        "intersects with rectangle (open)" - {
          open.rectangleIntersectCheck(intersectingRectangle) ==> true
          open.rectangleIntersectCheck(noneIntersectingRectangle) ==> false
          open.rectangleIntersectCheck(intersectingRectangleWithClosed) ==> false
        }

        "intersects with rectangle (closed)" - {
          closed.rectangleIntersectCheck(intersectingRectangle) ==> true
          closed.rectangleIntersectCheck(noneIntersectingRectangle) ==> false
          closed.rectangleIntersectCheck(intersectingRectangleWithClosed) ==> true
        }

        "intersects with polygon (open)" - {
          open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)) ==> true
          open.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)) ==> false
          open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)) ==> false
        }

        "intersects with polygon (closed)" - {
          closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)) ==> true
          closed.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)) ==> false
          closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)) ==> true
        }
      }

    }

}
