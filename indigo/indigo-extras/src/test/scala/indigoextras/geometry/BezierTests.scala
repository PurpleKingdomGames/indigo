package indigoextras.geometry

import indigo.shared.time.Seconds

class BezierTests extends munit.FunSuite {

  test("Interpolation") {
    assertEquals(Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 0d) == Vertex(0, 0), true)
    assertEquals(Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 0.5d) == Vertex(5, 5), true)
    assertEquals(Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 1d) == Vertex(10, 10), true)
  }

  test("Reduction.Empty list") {
    assertEquals(Bezier.reduce(Nil, 0d) == Vertex.zero, true)
  }

  test("Reduction.one point") {
    assertEquals(Bezier.reduce(List(Vertex(1, 1)), 0d) == Vertex(1, 1), true)
  }

  test("Reduction.two points") {
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 0d) == Vertex(0, 0), true)
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 0.5d) == Vertex(5, 5), true)
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 1d) == Vertex(10, 10), true)
  }

  test("Reduction.three points") {
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 0d) == Vertex(0, 0), true)
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 0.5d) == Vertex(5, 2.5), true)
    assertEquals(Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 1d) == Vertex(10, 0), true)
  }

  test("One dimensional (1 point)") {

    val bezier =
      Bezier(Vertex(5, 5))

    assertEquals(bezier.at(0d) == Vertex(5, 5), true)
    assertEquals(bezier.at(0d) == Bezier.atUnspecialised(bezier, 0d), true)

    assertEquals(bezier.at(0.5d) == Vertex(5, 5), true)
    assertEquals(bezier.at(0.5d) == Bezier.atUnspecialised(bezier, 0.5d), true)

    assertEquals(bezier.at(1d) == Vertex(5, 5), true)
    assertEquals(bezier.at(1d) == Bezier.atUnspecialised(bezier, 1d), true)

  }

  test("Linear (2 points)") {

    val bezier =
      Bezier(Vertex(0, 0), Vertex(10, 10))

    assertEquals(bezier.at(-50d) == Vertex(0, 0), true)
    assertEquals(bezier.at(-50d) == Bezier.atUnspecialised(bezier, -50d), true)

    assertEquals(bezier.at(0d) == Vertex(0, 0), true)
    assertEquals(bezier.at(0d) == Bezier.atUnspecialised(bezier, 0d), true)

    assertEquals(bezier.at(0.25d) == Vertex(2.5, 2.5), true)
    assertEquals(bezier.at(0.25d) == Bezier.atUnspecialised(bezier, 0.25d), true)

    assertEquals(bezier.at(0.5d) == Vertex(5, 5), true)
    assertEquals(bezier.at(0.5d) == Bezier.atUnspecialised(bezier, 0.5d), true)

    assertEquals(bezier.at(0.75d) == Vertex(7.5, 7.5), true)
    assertEquals(bezier.at(0.75d) == Bezier.atUnspecialised(bezier, 0.75d), true)

    assertEquals(bezier.at(1d) == Vertex(10, 10), true)
    assertEquals(bezier.at(1d) == Bezier.atUnspecialised(bezier, 1d), true)

    assertEquals(bezier.at(100d) == Vertex(10, 10), true)
    assertEquals(bezier.at(100d) == Bezier.atUnspecialised(bezier, 100d), true)

  }

  test("Quadtratic (3 points)") {

    val bezier =
      Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10))

    /*
          For 0.5d:

          2,2 4,7 20,10
          (2,2 4,7) (4,7 20,10)
          (3,4.5 12,8.5)
          7.5,6.5

     */

    assertEquals(bezier.at(0d) == Vertex(2, 2), true)
    assertEquals(bezier.at(0d) == Bezier.atUnspecialised(bezier, 0d), true)

    assertEquals(bezier.at(0.5d) == Vertex(7.5, 6.5), true)
    assertEquals(bezier.at(0.5d) == Bezier.atUnspecialised(bezier, 0.5d), true)

    assertEquals(bezier.at(1d) == Vertex(20, 10), true)

  }

  test("Cubic (4 points)") {

    val bezier =
      Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

    /*
          For 0.5d:

          2,2 4,7 20,10 3,100
          (2,2 4,7) (4,7 20,10) (20,10 3,100)
          (3,4.5 12,8.5) (12,8.5 [((3-20)/2)+20 = 11],55)
          (7.5,6.5 11.5,31.5)

          [((11.5-7.5)/2)+7.5 = 9.5] , ((31.5-6.5)/2)+6.5 = 19

     */

    assertEquals(bezier.at(0d) == Vertex(2, 2), true)
    assertEquals(bezier.at(0d) == Bezier.atUnspecialised(bezier, 0d), true)

    assertEquals(bezier.at(0.5d) == Vertex(9.625, 19.125), true)
    assertEquals(bezier.at(0.5d) == Bezier.atUnspecialised(bezier, 0.5d), true)

    assertEquals(bezier.at(1d) == Vertex(3, 100), true)
    assertEquals(bezier.at(1d) == Bezier.atUnspecialised(bezier, 1d), true)

  }

  test("to points.linear") {
    val bezier =
      Bezier(Vertex.zero, Vertex(100, 0))

    val actual = bezier.toVertices(10)

    val expected =
      List(
        Vertex(0, 0),
        Vertex(10, 0),
        Vertex(20, 0),
        Vertex(30, 0),
        Vertex(40, 0),
        Vertex(50, 0),
        Vertex(60, 0),
        Vertex(70, 0),
        Vertex(80, 0),
        Vertex(90, 0),
        Vertex(100, 0)
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual.zip(expected).forall(vs => vs._1 ~== vs._2), true)
  }

  test("to points.higher order") {
    val bezier =
      Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

    val actual = bezier.toVertices(2)

    val expected =
      List(
        Vertex(2, 2),
        Vertex(9.625, 19.125),
        Vertex(3, 100)
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual.zip(expected).forall(vs => vs._1 ~== vs._2), true)
  }

  test("to polygon.linear") {
    val bezier =
      Bezier(Vertex.zero, Vertex(100, 0))

    val actual: Int = bezier.toPolygon(10).edgeCount

    val expected: Int = 10

    assertEquals(actual, expected)
  }

  test("to line segments") {
    val bezier =
      Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

    val lineSegments = bezier.toLineSegments(2)

    assertEquals(lineSegments.length, 2)
    assertEquals(lineSegments(0) == LineSegment(Vertex(2, 2), Vertex(9.625, 19.125)), true)
    assertEquals(lineSegments(1) == LineSegment(Vertex(9.625, 19.125), Vertex(3, 100)), true)
  }

  test("to signal") {
    val bezier =
      Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

    val signal =
      bezier.toSignal(Seconds(1.5))

    assertEquals(signal.at(Seconds(0)) == Vertex(2, 2), true)
    assertEquals(signal.at(Seconds(0.75)) == Vertex(9.625, 19.125), true)
    assertEquals(signal.at(Seconds(1.5)) == Vertex(3, 100), true)
  }

  test("give bounding rectangle") {

    val bezier =
      Bezier(Vertex(20, 10), Vertex(3, 100), Vertex(2, 2))

    val actual =
      bezier.bounds

    val expected: BoundingBox =
      BoundingBox(2, 2, 18.001, 98.001)

    assertEquals(actual == expected, true)
  }

}
