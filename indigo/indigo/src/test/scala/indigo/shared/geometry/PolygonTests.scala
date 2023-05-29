package indigo.shared.geometry

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

class PolygonTests extends munit.FunSuite {

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
    Rectangle(Point(0, 5), Size(5, 0))

  val noneIntersectingRectangle: Rectangle =
    Rectangle(Point(0, 5), Size(1, 1))

  val intersectingRectangleWithClosed: Rectangle =
    Rectangle(Point(0, -5), Size(4, 4))

  test("Construction.should be able to create an open polygon") {
    assertEquals(open.edgeCount, 3)
  }

  test("Construction.should be able to create a closed polygon") {
    assertEquals(closed.edgeCount, 4)
  }

  test("Construction.should be able to add a vertex") {
    assertEquals(Polygon.Open.empty.addVertex(Vertex.zero).vertices.length, 1)
  }

  test("Construction.should be able to produce a bounding rectangle") {
    assertEquals(closed.bounds.toRectangle == Rectangle(0, -5, 10, 10), true)
  }

  test("Construction.should be able to create a polygon from a rectangle") {
    val expected: Polygon =
      Polygon.Closed(
        Vertex(0, 0),
        Vertex(0, 10),
        Vertex(10, 10),
        Vertex(10, 0)
      )

    val actual: Polygon =
      Polygon.fromRectangle(Rectangle(Point(0, 0), Size(10, 10)))

    assertEquals(expected == actual, true)
  }

  test("Operations.should be able to produce line segments (open)") {

    val expected: Batch[LineSegment] =
      Batch(
        LineSegment(Vertex(0, 0), Vertex(5, 5)),
        LineSegment(Vertex(5, 5), Vertex(10, 0)),
        LineSegment(Vertex(10, 0), Vertex(5, -5))
      )

    val actual: Batch[LineSegment] =
      open.lineSegments

    assertEquals(actual, expected)
  }

  test("Operations.should be able to produce line segments (closed)") {

    val expected: Batch[LineSegment] =
      Batch(
        LineSegment(Vertex(0, 0), Vertex(5, 5)),
        LineSegment(Vertex(5, 5), Vertex(10, 0)),
        LineSegment(Vertex(10, 0), Vertex(5, -5)),
        LineSegment(Vertex(5, -5), Vertex(0, 0))
      )

    val actual: Batch[LineSegment] =
      closed.lineSegments

    assertEquals(actual, expected)
  }

  test("Intersections.contains point (open shapes can't contain)") {
    assertEquals(open.contains(Vertex(2, 1)), false)
  }

  test("Intersections.contains point (closed).totally enclosed") {
    assertEquals(closed.contains(Vertex(2, 0)), true)
  }

  test("Intersections.contains point (closed).within bounds but not inside polygon") {
    assertEquals(closed.contains(Vertex(1, 4)), false)
  }

  test("Intersections.intersects with line (open)") {
    assertEquals(open.lineIntersectCheck(intersectingLine), true)
    assertEquals(open.lineIntersectCheck(noneIntersectingLine), false)
    assertEquals(open.lineIntersectCheck(intersectingLineWithClosed), false)
  }

  test("Intersections.intersects with line (closed)") {
    assertEquals(closed.lineIntersectCheck(intersectingLine), true)
    assertEquals(closed.lineIntersectCheck(noneIntersectingLine), false)
    assertEquals(closed.lineIntersectCheck(intersectingLineWithClosed), true)
  }

  test("Intersections.intersects with rectangle (open)") {
    assertEquals(open.rectangleIntersectCheck(intersectingRectangle), true)
    assertEquals(open.rectangleIntersectCheck(noneIntersectingRectangle), false)
    assertEquals(open.rectangleIntersectCheck(intersectingRectangleWithClosed), false)
  }

  test("Intersections.intersects with rectangle (closed)") {
    assertEquals(closed.rectangleIntersectCheck(intersectingRectangle), true)
    assertEquals(closed.rectangleIntersectCheck(noneIntersectingRectangle), false)
    assertEquals(closed.rectangleIntersectCheck(intersectingRectangleWithClosed), true)
  }

  test("Intersections.intersects with polygon (open)") {
    assertEquals(open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)), true)
    assertEquals(open.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)), false)
    assertEquals(open.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)), false)
  }

  test("Intersections.intersects with polygon (closed)") {
    assertEquals(closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangle)), true)
    assertEquals(closed.polygonIntersectCheck(Polygon.fromRectangle(noneIntersectingRectangle)), false)
    assertEquals(closed.polygonIntersectCheck(Polygon.fromRectangle(intersectingRectangleWithClosed)), true)
  }

  test("moveTo") {
    val actual =
      closed.moveTo(10, 20)

    val expected =
      Polygon.Closed(
        Vertex(10, 20),
        Vertex(15, 25),
        Vertex(20, 20),
        Vertex(15, 15)
      )

    assertEquals(actual, expected)
  }

  test("moveBy") {
    val actual =
      closed.moveBy(100, 50)

    val expected =
      Polygon.Closed(
        Vertex(100, 50),
        Vertex(105, 55),
        Vertex(110, 50),
        Vertex(105, 45)
      )

    assertEquals(actual, expected)
  }

  test("scaleBy") {
    val amount = 10.0d

    val actual =
      closed.scaleBy(amount)

    val expected =
      Polygon.Closed(
        Vertex(0 * amount, 0 * amount),
        Vertex(5 * amount, 5 * amount),
        Vertex(10 * amount, 0 * amount),
        Vertex(5 * amount, -5 * amount)
      )

    assertEquals(actual, expected)
  }

}
