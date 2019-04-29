package indigoexts.geometry

import indigo.shared.datatypes.Point

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.EqualTo._

object PolygonTests extends TestSuite {

  // Polygons
  val open: Polygon.Open =
    Polygon.Open(
      Point(0, 0),
      Point(5, 5),
      Point(10, 0),
      Point(-5, -5)
    )

  val closed: Polygon.Closed =
    Polygon.Closed(
      Point(0, 0),
      Point(5, 5),
      Point(10, 0),
      Point(-5, -5)
    )

  // Lines
  val intersectingLine: LineSegment =
    LineSegment(Point(0, 5), Point(5, 0))

  val noneIntersectingLine: LineSegment =
    LineSegment(Point(0, 5), Point(1, 1))

  val intersectingLineWithClosed: LineSegment =
    LineSegment(Point(0, -5), Point(5, 0))

  // Rectangles
  val intersectingRectangle: Rectangle =
    Rectangle(Point(0, 5), Point(5, 0))

  val noneIntersectingRectangle: Rectangle =
    Rectangle(Point(0, 5), Point(1, 1))

  val intersectingRectangleWithClosed: Rectangle =
    Rectangle(Point(0, -5), Point(5, 0))

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
          Polygon.Open.empty.addVertex(Point.zero).vertices.length ==> 1
        }

        "should be able to produce a bounding rectangle" - {
          closed.bounds === Rectangle(0, 5, 10, 10) ==> true
        }

        "should be able to create a polygon from a rectangle" - {
          val expected: Polygon.Closed = 
            Polygon.Closed(
              Point(0, 0),
              Point(10, 0),
              Point(10, 10),
              Point(0, 10)
            )

          val actual: Polygon.Closed = 
          Polygon.fromRectangle(Rectangle(Point(0, 0), Point(10, 10)))

          expected === actual ==> true
        }
      }

      "Intersections" - {
        "contains point (open)" - {
          open.contains(Point(2, 1)) ==> true
        }

        "contains point (closed)" - {
          closed.contains(Point(2, 1)) ==> true
        }

        "intersets with line (open)" - {
          open.lineIntersectCheck(intersectingLine) ==> true
          open.lineIntersectCheck(noneIntersectingLine) ==> false
          open.lineIntersectCheck(intersectingLineWithClosed) ==> false
        }

        "intersets with line (closed)" - {
          closed.lineIntersectCheck(intersectingLine) ==> true
          closed.lineIntersectCheck(noneIntersectingLine) ==> false
          closed.lineIntersectCheck(intersectingLineWithClosed) ==> true
        }

        "intersets with rectangle (open)" - {
          open.rectangleIntersectCheck(intersectingRectangle) ==> true
          open.rectangleIntersectCheck(noneIntersectingRectangle) ==> false
          open.rectangleIntersectCheck(intersectingRectangleWithClosed) ==> false
        }

        "intersets with rectangle (closed)" - {
          closed.rectangleIntersectCheck(intersectingRectangle) ==> true
          closed.rectangleIntersectCheck(noneIntersectingRectangle) ==> false
          closed.rectangleIntersectCheck(intersectingRectangleWithClosed) ==> true
        }

        "intersets with polygon (open)" - {
          open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)) ==> true
          open.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)) ==> false
          open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)) ==> false
        }

        "intersets with polygon (closed)" - {
          closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)) ==> true
          closed.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)) ==> false
          closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)) ==> true
        }
      }

    }

}
