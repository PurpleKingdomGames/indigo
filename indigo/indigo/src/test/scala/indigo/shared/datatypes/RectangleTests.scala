package indigo.shared.datatypes

class RectangleTests extends munit.FunSuite {

  test("should be able to construct a rectangle from two points") {
    val pt1 = Point(5, 6)
    val pt2 = Point(1, 3)

    val expected = Rectangle(1, 3, 4, 3)

    assertEquals(Rectangle.fromPoints(pt1, pt2), expected)
  }

  test("should be able to construct a rectangle from three points") {
    val pt1 = Point(3, 1)
    val pt2 = Point(1, 3)
    val pt3 = Point(2, 5)

    val expected = Rectangle(1, 1, 2, 4)

    assertEquals(Rectangle.fromPoints(pt1, pt2, pt3), expected)
  }

  test("should be able to construct a rectangle from four points") {
    val pt1 = Point(5, 6)
    val pt2 = Point(1, 3)
    val pt3 = Point(3, 1)
    val pt4 = Point(2, 5)

    val expected = Rectangle(1, 1, 4, 5)

    assertEquals(Rectangle.fromPoints(pt1, pt2, pt3, pt4), expected)
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

    assertEquals(actual, expected)
  }

  test(
    "Expand to include two rectangles.should return the original rectangle when it already encompasses the second one"
  ) {
    val a = Rectangle(10, 20, 100, 200)
    val b = Rectangle(20, 20, 50, 50)

    assertEquals(Rectangle.expandToInclude(a, b), a)
  }

  test("Expand to include two rectangles.should expand to meet the bounds of both") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(100, 100, 100, 100)

    assertEquals(Rectangle.expandToInclude(a, b), Rectangle(10, 10, 190, 190))
  }

  test("expand a rectangle with negative size") {
    val a = Rectangle(10, 10, -20, -20)

    assertEquals(a.expand(10), Rectangle(20, 20, -40, -40))
  }

  test("expand a rectangle to include another rectangle with negative size") {
    val a = Rectangle(50, 50, -20, -20)
    val b = Rectangle(100, 100, 100, 100)

    assertEquals(Rectangle.expandToInclude(a, b), Rectangle(30, 30, 170, 170))
  }

  test("expand a rectangle with negative start position") {
    val a = Rectangle(-10, -10, 20, -20)

    assertEquals(a.expand(10), Rectangle(-20, 0, 40, -40))
  }

  test("contract by a fixed amount") {
    val actual =
      Rectangle(10, 20, 90, 80).contract(10)

    val expected =
      Rectangle(20, 30, 70, 60)

    assertEquals(actual, expected)
  }

  test("contract by a fixed amount (negative)") {
    val actual =
      Rectangle(-10, -20, 90, -80).contract(10)

    val expected =
      Rectangle(0, -30, 70, -60)

    assertEquals(actual, expected)
  }

  test("intersecting points.should be able to detect if the point is inside the Rectangle") {
    assert(Rectangle(0, 0, 10, 10).isPointWithin(Point(5, 5)))
    assert(Rectangle(0, 0, 10, 10).contains(Point(5, 5)))
  }

  test("intersecting points.should be able to detect that a point is outside the Rectangle") {
    assert(!Rectangle(0, 0, 10, 10).isPointWithin(Point(20, 5)))
    assert(!Rectangle(0, 0, 10, 10).contains(Point(20, 5)))
  }

  test("encompasing rectangles.should return true when A encompases B") {
    val a = Rectangle(10, 10, 110, 110)
    val b = Rectangle(20, 20, 10, 10)

    assert(Rectangle.encompassing(a, b))
  }

  test("encompasing rectangles.should return true when A encompases B and B has a negative size") {
    val a = Rectangle(10, 10, 110, 110)
    val b = Rectangle(30, 30, -10, -10)

    assert(Rectangle.encompassing(a, b))
  }

  test("encompasing rectangles.should return false when A does not encompass B") {
    val a = Rectangle(20, 20, 10, 10)
    val b = Rectangle(10, 10, 110, 110)

    assert(!Rectangle.encompassing(a, b))
  }

  test("encompasing rectangles.should return false when A and B merely intersect") {
    val a = Rectangle(10, 10, 20, 200)
    val b = Rectangle(15, 15, 100, 10)

    assert(!Rectangle.encompassing(a, b))
  }

  test("overlapping rectangles.should return true when A overlaps B") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(15, 15, 100, 100)

    assert(Rectangle.overlapping(a, b))
  }

  test("overlapping rectangles.should return false when A and B do not overlap") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(100, 100, 100, 100)

    assert(!Rectangle.overlapping(a, b))
  }

  test("overlapping rectangles.should return true when A overlaps B and A has a negative size.") {
    val a = Rectangle(105, 105, -20, -20)
    val b = Rectangle(10, 10, 90, 90)

    assert(Rectangle.overlapping(a, b))
  }

  test("overlapping rectangles.should return false when A and B do not overlap and A has a negative size.") {
    val a = Rectangle(125, 125, -10, -10)
    val b = Rectangle(10, 10, 90, 90)

    assert(!Rectangle.overlapping(a, b))
  }

  test("overlapping rectangles.should return true when A is more than 1 in height, but B is 1 in height.") {
    val a = Rectangle(3, 5, 2, 2)
    val b = Rectangle(4, 5, 2, 1)

    assert(Rectangle.overlapping(a, b))
  }

  test("Expand should be able to expand in size by a given amount") {
    val a = Rectangle(10, 10, 20, 20)
    val b = Rectangle(0, 10, 100, 5)

    assertEquals(Rectangle.expand(a, 10), Rectangle(0, 0, 40, 40))
    assertEquals(Rectangle.expand(b, 50), Rectangle(-50, -40, 200, 105))
  }

  test("should be able to find edges (positive)") {
    val a = Rectangle(10, 20, 30, 40)

    assert(a.left == 10)
    assert(a.right == 40)
    assert(a.top == 20)
    assert(a.bottom == 60)
  }

  test("should be able to find edges (negative)") {
    val a = Rectangle(10, 20, -30, -40)

    assert(a.left == -20)
    assert(a.right == 10)
    assert(a.top == -20)
    assert(a.bottom == 20)
  }
}
