package indigo.shared.datatypes

class PointTests extends munit.FunSuite {

  test("should be able to calculate a linear interpolation") {

    assertEquals(Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 5) == Point(15, 15), true)
    assertEquals(Point.linearInterpolation(Point(10, 20), Point(20, 20), 10, 5) == Point(15, 20), true)
    assertEquals(Point.linearInterpolation(Point(20, 10), Point(20, 20), 10, 5) == Point(20, 15), true)
    assertEquals(Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 1) == Point(11, 11), true)
    assertEquals(Point.linearInterpolation(Point(10, 10), Point(-10, -10), 100, 99) == Point(-9, -9), true)

  }

  test("should be able to calculate the distance between horizontal points") {
    val p1: Point = Point(10, 10)
    val p2: Point = Point(30, 10)

    assertEquals(p1.distanceTo(p2), 20.0)
  }

  test("should be able to calculate the distance between vertical points") {
    val p1: Point = Point(10, 10)
    val p2: Point = Point(10, 30)

    assertEquals(p1.distanceTo(p2), 20.0)
  }

  test("should be able to calculate the distance between diagonal points") {
    val p1: Point = Point(10, 10)
    val p2: Point = Point(30, 30)

    assertEquals(p1.distanceTo(p2), Math.sqrt((20d * 20d) + (20d * 20d)))
  }

}
