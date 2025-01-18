package indigo.shared.geometry

import indigo.shared.geometry.LineIntersectionResult.*

class LineTests extends munit.FunSuite {

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */

  // Line calculations

  test("calculating line components.should correctly calculate the line components.(0, 0) -> (2, 2)") {
    val expected: Line.Components = Line.Components(1, 0)

    assertEquals(Line.fromLineSegment(LineSegment(Vertex(0, 0), Vertex(2, 2))), expected)
  }

  test("calculating line components.should correctly calculate the line components.(2, 1) -> (3, 4)") {
    val expected: Line.Components = Line.Components(3, -5)

    assertEquals(Line.fromLineSegment(LineSegment(Vertex(2, 1), Vertex(3, 4))), expected)
  }

  test("calculating line components.should correctly calculate the line components.(2, 2) -> (2, -3)") {
    // Does not work because you can't have m of 0 or b of infinity)
    // i.e. lines parallel to x or y axis
    // We're also getting a divide by 0 because ... / x - x = 0
    val expected: Line = Line.ParallelToAxisY(2)

    assertEquals(Line.fromLineSegment(LineSegment(Vertex(2, 2), Vertex(2, -3))), expected)
  }

  test("calculating line components.should correctly identify a line parallel to the y-axis") {
    // b = Infinity (or -Infinity)
    assertEquals(Line.fromLineSegment(LineSegment(Vertex(1, 2), Vertex(1, -3))), Line.ParallelToAxisY(1))
  }

  test("line intersections.should not intersect with a parallel lines") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((-3d, 3d), (2d, 3d)).toLine,
      LineSegment((1d, 1d), (-2d, 1d)).toLine
    )

    val expected = NoIntersection

    assertEquals(actual, expected)
  }

  test("line intersections.should not intersect with a parallel lines (faster)") {
    val actual: Boolean =
      LineSegment((-3d, 3d), (2d, 3d)).toLine
        .intersectsWith(LineSegment((1d, 1d), (-2d, 1d)).toLine)

    val expected = Line.intersection(
      LineSegment((-3d, 3d), (2d, 3d)).toLine,
      LineSegment((1d, 1d), (-2d, 1d)).toLine
    )

    assertEquals(actual, expected.hasIntersected)
  }

  test("line intersections.should intersect lines at right angles to each other") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((2d, 2d), (2d, -3d)).toLine,
      LineSegment((-1d, -2d), (3d, -2d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(2, -2)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect lines at right angles to each other (faster)") {
    val actual: Option[Vertex] =
      LineSegment((2d, 2d), (2d, -3d)).toLine
        .intersectsAt(
          LineSegment((-1d, -2d), (3d, -2d)).toLine
        )

    val expected: Option[Vertex] = Line
      .intersection(
        LineSegment((2d, 2d), (2d, -3d)).toLine,
        LineSegment((-1d, -2d), (3d, -2d)).toLine
      )
      .toOption

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally right angle lines") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((1d, 1d), (5d, 5d)).toLine,
      LineSegment((1d, 5d), (4d, 2d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(3, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally right angle lines (faster)") {
    val actual =
      LineSegment((1d, 1d), (5d, 5d)).toLine
        .intersectsAt(
          LineSegment((1d, 5d), (4d, 2d)).toLine
        )

    val expected = Line
      .intersection(
        LineSegment((1d, 1d), (5d, 5d)).toLine,
        LineSegment((1d, 5d), (4d, 2d)).toLine
      )
      .toOption

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally non-right angle lines") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((1d, 5d), (3d, 1d)).toLine,
      LineSegment((1d, 2d), (4d, 5d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(2, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally non-right angle lines (faster)") {
    val actual =
      LineSegment((1d, 5d), (3d, 1d)).toLine
        .intersectsAt(LineSegment((1d, 2d), (4d, 5d)).toLine)

    val expected = Line
      .intersection(
        LineSegment((1d, 5d), (3d, 1d)).toLine,
        LineSegment((1d, 2d), (4d, 5d)).toLine
      )
      .toOption

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the y-axis") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((4d, 1d), (4d, 4d)).toLine,
      LineSegment((2d, 1d), (5d, 4d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(4, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the y-axis (faster)") {
    val actual =
      LineSegment((4d, 1d), (4d, 4d)).toLine
        .intersectsAt(
          LineSegment((2d, 1d), (5d, 4d)).toLine
        )

    val expected = Line
      .intersection(
        LineSegment((4d, 1d), (4d, 4d)).toLine,
        LineSegment((2d, 1d), (5d, 4d)).toLine
      )
      .toOption

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the x-axis") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((1d, 2d), (5d, 2d)).toLine,
      LineSegment((2d, 4d), (5d, 1d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(4, 2)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the x-axis (faster)") {
    val actual =
      LineSegment((1d, 2d), (5d, 2d)).toLine
        .intersectsAt(
          LineSegment((2d, 4d), (5d, 1d)).toLine
        )

    val expected = Line
      .intersection(
        LineSegment((1d, 2d), (5d, 2d)).toLine,
        LineSegment((2d, 4d), (5d, 1d)).toLine
      )
      .toOption

    assertEquals(actual, expected)
  }

  test("line intersections.should give the same intersection regardless of order") {
    val actual1: LineIntersectionResult = Line.intersection(
      LineSegment((0d, 15d), (50d, 15d)).toLine,
      LineSegment((10d, 10d), (10d, 30d)).toLine
    )

    val actual2: LineIntersectionResult = Line.intersection(
      LineSegment((0d, 15d), (50d, 15d)).toLine,
      LineSegment((10d, 10d), (10d, 30d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(10, 15)

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
    assertEquals(actual1, actual2)
  }

  test("line intersections.should intersect diagonally right angle lines (again)") {
    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((0d, 0d), (5d, 5d)).toLine,
      LineSegment((0d, 5d), (5d, 0d)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(2.5f, 2.5f)

    assertEquals(actual, expected)
  }

  test("line intersections.Intersection use case A") {

    val actual: LineIntersectionResult = Line.intersection(
      LineSegment((0.0, 0.0), (5.0, 5.0)).toLine,
      LineSegment((0.0, 3.0), (5.0, 3.0)).toLine
    )

    val expected: IntersectionVertex = IntersectionVertex(3d, 3d)

    assertEquals(actual, expected)

  }

  test("line intersections.Intersection use case B") {
    val lineA = LineSegment((0.0, 0.0), (0.0, 5.0)).toLine
    val lineB = LineSegment((0.0, 0.5), (5.0, 3.0)).toLine

    val actual: LineIntersectionResult = Line.intersection(
      lineA,
      lineB
    )

    val expected: IntersectionVertex = IntersectionVertex(0.0, 0.5)

    assertEquals(actual, expected)
  }

  test("Approximately equal") {
    assert(LineSegment((-3d, 3d), (2d, 3d)).toLine ~== LineSegment((-3d, 3d), (2d, 3d)).toLine)
    assert(LineSegment((-3.000001d, 3d), (2d, 3d)).toLine ~== LineSegment((-2.999d, 3d), (2d, 3d)).toLine)
    assert(!(LineSegment((-3d, 3d), (2d, 3d)).toLine ~== LineSegment((-3d, 5d), (2d, 3d)).toLine))
  }

}
