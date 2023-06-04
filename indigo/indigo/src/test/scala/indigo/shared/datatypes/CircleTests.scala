package indigo.shared.datatypes

import indigo.shared.collections.Batch

class CircleTests extends munit.FunSuite {

  test("contains") {
    val c = Circle(Point(20, 20), 10)

    assert(c.contains(Point(15, 15)))
    assert(!c.contains(Point.zero))
  }

  test("expandToInclude") {
    val c1 = Circle(Point(20, 20), 10)
    val c2 = Circle(Point(40, 20), 5)
    val c3 = Circle(Point(20, 20), 25)

    assertEquals(c1.expandToInclude(c2), c3)
  }

  test("encompasses") {
    val c1 = Circle(Point(20, 20), 10)
    val c2 = Circle(Point(40, 20), 5)
    val c3 = Circle(Point(20, 20), 25)

    assert(c3.encompasses(c1))
    assert(c3.encompasses(c2))
    assert(!c1.encompasses(c2))
  }

  test("overlaps") {
    val c1 = Circle(Point(20, 20), 10)
    val c2 = Circle(Point(25, 25), 10)
    val c3 = Circle(Point(10, 41), 10)

    assert(c1 overlaps c2)
    assert(!(c1 overlaps c3))
  }

  test("moveBy | moveTo") {
    val c = Circle(Point(20, 20), 10)

    assertEquals(c.moveBy(1, 2).position, Point(21, 22))
    assertEquals(c.moveTo(1, 2).position, Point(1, 2))
  }

  test("resize") {
    val c = Circle(Point(20, 20), 10)

    assertEquals(c.resize(5).radius, 5)
  }

  test("Constructor - fromTwoVertices") {
    val c = Circle.fromTwoPoints(Point(5, 10), Point(5, -10))

    assertEquals(c.position, Point(5, 10))
    assertEquals(c.radius, 20)
  }

  test("Constructor - fromVertices") {
    val actual =
      Circle.fromPointCloud(
        Batch(
          Point(10, 10), // tl
          Point(20, 10), // tr
          Point(20, 20), // br
          Point(10, 20)  // bl
        )
      )

    val expected =
      Circle(Point(15, 15), Point(15, 15).distanceTo(Point(10, 10)).toInt)

    assertEquals(actual.position, expected.position)
    assert(doubleCloseEnough(actual.radius, expected.radius))
  }

  test("signed distance function") {
    val c = Circle(Point(20, 20), 10)

    // top
    assertEquals(c.sdf(Point(20, 1)), 9)

    // bottom
    assertEquals(c.sdf(Point(20, 55)), 25)

    // left
    assertEquals(c.sdf(Point(-10, 20)), 20)

    // right
    assertEquals(c.sdf(Point(33, 20)), 3)

    // edge
    assertEquals(c.sdf(Point(10, 20)), 0)

    // inside
    assertEquals(c.sdf(Point(12, 20)), -2)

    // Diagonal
    assertEquals(c.sdf(Point.zero), 18)

  }

  def doubleCloseEnough(r1: Double, r2: Double): Boolean =
    r1 - 0.001 < r2 && r1 + 0.001 > r2

}
