package indigo.shared.datatypes

import indigo.shared.EqualTo._

class RectangleTests extends munit.FunSuite {

  test("should be able to construct a rectangle from two points") {
    val pt1 = Point(5, 6)
    val pt2 = Point(1, 3)

    val expected = Rectangle(1, 3, 4, 3)

    assertEquals(Rectangle.fromTwoPoints(pt1, pt2) === expected, true)
  }

  test("should be able to construct a rectangle from a cloud of points") {
    //left 0, right 6, top 7, bottom 13
    val points: List[Point] =
      List(
        Point(4, 11),
        Point(6, 8),
        Point(2, 9),
        Point(1, 13),
        Point(3, 10),
        Point(0, 12),
        Point(5, 7)
      )

    val expected: Rectangle =
      Rectangle(0, 7, 6, 6)

    val actual: Rectangle =
      Rectangle.fromPointCloud(points)

    assertEquals(actual === expected, true)
  }

  test("Expand to include two rectangles.should return the original rectangle when it already encompasses the second one") {
    val a = Rectangle(10, 20, 100, 200)
    val b = Rectangle(20, 20, 50, 50)

    assertEquals(Rectangle.expandToInclude(a, b) === a, true)
  }

  test("Expand to include two rectangles.should expand to meet the bounds of both") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(100, 100, 100, 100)

    assertEquals(Rectangle.expandToInclude(a, b) === Rectangle(10, 10, 190, 190), true)
  }

  test("intersecting points.should be able to detect if the point is inside the Rectangle") {
    assertEquals(Rectangle(0, 0, 10, 10).isPointWithin(Point(5, 5)), true)
  }

  test("intersecting points.should be able to detect that a point is outside the Rectangle") {
    assertEquals(Rectangle(0, 0, 10, 10).isPointWithin(Point(20, 5)), false)
  }

  test("encompasing rectangles.should return true when A encompases B") {
    val a = Rectangle(10, 10, 110, 110)
    val b = Rectangle(20, 20, 10, 10)

    assertEquals(Rectangle.encompassing(a, b), true)
  }

  test("encompasing rectangles.should return false when A does not encompass B") {
    val a = Rectangle(20, 20, 10, 10)
    val b = Rectangle(10, 10, 110, 110)

    assertEquals(Rectangle.encompassing(a, b), false)
  }

  test("encompasing rectangles.should return false when A and B merely intersect") {
    val a = Rectangle(10, 10, 20, 200)
    val b = Rectangle(15, 15, 100, 10)

    assertEquals(Rectangle.encompassing(a, b), false)
  }

  test("overlapping rectangles.should return true when A overlaps B") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(15, 15, 100, 100)

    assertEquals(Rectangle.overlapping(a, b), true)
  }

  test("overlapping rectangles.should return false when A and B do not overlap") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(100, 100, 100, 100)

    assertEquals(Rectangle.overlapping(a, b), false)
  }

  test("Expand should be able to expand in size by a given amount") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(0, 10, 100, 5)

    assertEquals(Rectangle.expand(a, 10) === Rectangle(0, 0, 40, 40), true)
    assertEquals(Rectangle.expand(b, 50) === Rectangle(-50, -40, 200, 105), true)
  }
}
