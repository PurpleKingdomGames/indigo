package indigo.shared.geometry

import indigo.shared.collections.Batch

class BoundingCircleTests extends munit.FunSuite {

  test("contains") {
    val c = BoundingCircle(Vertex(20, 20), 10)

    assert(c.contains(Vertex(15, 15)))
    assert(!c.contains(Vertex.zero))
  }

  test("expandToInclude") {
    val c1 = BoundingCircle(Vertex(20, 20), 10)
    val c2 = BoundingCircle(Vertex(40, 20), 5)
    val c3 = BoundingCircle(Vertex(20, 20), 25)

    assertEquals(c1.expandToInclude(c2), c3)
  }

  test("encompasses") {
    val c1 = BoundingCircle(Vertex(20, 20), 10)
    val c2 = BoundingCircle(Vertex(40, 20), 5)
    val c3 = BoundingCircle(Vertex(20, 20), 25)

    assert(c3.encompasses(c1))
    assert(c3.encompasses(c2))
    assert(!c1.encompasses(c2))
  }

  test("overlaps") {
    val c1 = BoundingCircle(Vertex(20, 20), 10)
    val c2 = BoundingCircle(Vertex(25, 25), 10)
    val c3 = BoundingCircle(Vertex(10, 40), 10)

    assert(c1 overlaps c2)
    assert(!(c1 overlaps c3))
  }

  test("moveBy | moveTo") {
    val c = BoundingCircle(Vertex(20, 20), 10)

    assertEquals(c.moveBy(1, 2).position, Vertex(21, 22))
    assertEquals(c.moveTo(1, 2).position, Vertex(1, 2))
  }

  test("resize") {
    val c = BoundingCircle(Vertex(20, 20), 10)

    assertEquals(c.resize(5).radius, 5.0d)
  }

  test("Constructor - fromTwoVertices") {
    val c = BoundingCircle.fromTwoVertices(Vertex(5, 10), Vertex(5, -10))

    assertEquals(c.position, Vertex(5, 10))
    assertEquals(c.radius, 20.0d)
  }

  test("Constructor - fromVertices") {
    val actual =
      BoundingCircle.fromVertices(
        Batch(
          Vertex(10, 10), // tl
          Vertex(20, 10), // tr
          Vertex(20, 20), // br
          Vertex(10, 20)  // bl
        )
      )

    val expected =
      BoundingCircle(Vertex(15, 15), Vertex(15, 15).distanceTo(Vertex(10, 10)))

    assert(clue(actual.position.round) ~== clue(expected.position))
    assert(doubleCloseEnough(actual.radius, expected.radius))
  }

  test("lineIntersects - miss") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 0), Vertex(5, 5))

    assert(!c.lineIntersects(l))
  }

  test("lineIntersects - one point") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 10), Vertex(50, 10))

    assert(c.lineIntersects(l))
  }

  test("lineIntersects - two points") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 15), Vertex(50, 15))

    assert(c.lineIntersects(l))
  }

  test("lineIntersectsAt - miss") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 0), Vertex(5, 5))

    assertEquals(c.lineIntersectsAt(l).toOption, None)
  }

  test("lineIntersectsAt - one point") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 10), Vertex(50, 10))

    val expected =
      Some(
        List(
          Vertex(20, 10)
        )
      )

    assertEquals(c.lineIntersectsAt(l).toOption, expected)
  }

  test("lineIntersectsAt - two points") {
    val c = BoundingCircle(Vertex(20, 20), 10)
    val l = LineSegment(Vertex(0, 15), Vertex(50, 15))

    val expected =
      Some(
        List(
          Vertex(11, 15),
          Vertex(29, 15)
        )
      )

    val actual =
      c.lineIntersectsAt(l)
        .toOption
        .map(_.map(_.round))

    assertEquals(actual, expected)
  }

  test("signed distance function") {
    val c = BoundingCircle(Vertex(20, 20), 10)

    // top
    assertEquals(c.sdf(Vertex(20, 1)), 9.0d)

    // bottom
    assertEquals(c.sdf(Vertex(20, 55)), 25.0d)

    // left
    assertEquals(c.sdf(Vertex(-10, 20)), 20.0d)

    // right
    assertEquals(c.sdf(Vertex(33, 20)), 3.0d)

    // edge
    assertEquals(c.sdf(Vertex(10, 20)), 0.0d)

    // inside
    assertEquals(c.sdf(Vertex(12, 20)), -2.0d)

    // Diagonal
    assertEquals(Math.round(c.sdf(Vertex.zero)).toDouble, 18.0d)

  }

  def doubleCloseEnough(r1: Double, r2: Double): Boolean =
    r1 - 0.001 < r2 && r1 + 0.001 > r2

}
