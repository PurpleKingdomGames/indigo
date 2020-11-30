package indigoextras.geometry

import indigo.shared.datatypes.Vector2
import indigoextras.geometry.IntersectionResult._
import indigoextras.geometry.LineProperties._
import indigo.shared.EqualTo._

class LineSegmentTests extends munit.FunSuite {

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */

  test("calculating line components.should correctly calculate the line components.(0, 0) -> (2, 2)") {
    val expected: LineComponents = LineComponents(1, 0)

    assertEquals(LineSegment.calculateLineComponents(Vertex(0, 0), Vertex(2, 2)), expected)
  }

  test("calculating line components.should correctly calculate the line components.(2, 1) -> (3, 4)") {
    val expected: LineComponents = LineComponents(3, -5)

    assertEquals(LineSegment.calculateLineComponents(Vertex(2, 1), Vertex(3, 4)), expected)
  }

  test("calculating line components.should correctly calculate the line components.(2, 2) -> (2, -3)") {
    // Does not work because you can't have m of 0 or b of infinity)
    // i.e. lines parallel to x or y axis
    // We're also getting a divide by 0 because ... / x - x = 0
    val expected = ParallelToAxisY

    assertEquals(LineSegment.calculateLineComponents(Vertex(2, 2), Vertex(2, -3)), expected)
  }

  test("calculating line components.should correctly identify a line parallel to the y-axis") {
    //b = Infinity (or -Infinity)
    assertEquals(LineSegment.calculateLineComponents(Vertex(1, 2), Vertex(1, -3)), ParallelToAxisY)
  }

  test("line intersections.should not intersect with a parallel lines") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((-3d, 3d), (2d, 3d)),
      LineSegment((1d, 1d), (-2d, 1d))
    )

    val expected = NoIntersection

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect lines at right angles to each other") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((2d, 2d), (2d, -3d)),
      LineSegment((-1d, -2d), (3d, -2d))
    )

    val expected: IntersectionVertex = IntersectionVertex(2, -2)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally right angle lines") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((1d, 1d), (5d, 5d)),
      LineSegment((1d, 5d), (4d, 2d))
    )

    val expected: IntersectionVertex = IntersectionVertex(3, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect diagonally non-right angle lines") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((1d, 5d), (3d, 1d)),
      LineSegment((1d, 2d), (4d, 5d))
    )

    val expected: IntersectionVertex = IntersectionVertex(2, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the y-axis") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((4d, 1d), (4d, 4d)),
      LineSegment((2d, 1d), (5d, 4d))
    )

    val expected: IntersectionVertex = IntersectionVertex(4, 3)

    assertEquals(actual, expected)
  }

  test("line intersections.should intersect where one line is parallel to the x-axis") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((1d, 2d), (5d, 2d)),
      LineSegment((2d, 4d), (5d, 1d))
    )

    val expected: IntersectionVertex = IntersectionVertex(4, 2)

    assertEquals(actual, expected)
  }

  test("line intersections.should give the same intersection regardless of order") {
    val actual1: IntersectionResult = LineSegment.intersection(
      LineSegment((0d, 15d), (50d, 15d)),
      LineSegment((10d, 10d), (10d, 30d))
    )

    val actual2: IntersectionResult = LineSegment.intersection(
      LineSegment((0d, 15d), (50d, 15d)),
      LineSegment((10d, 10d), (10d, 30d))
    )

    val expected: IntersectionVertex = IntersectionVertex(10, 15)

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
    assertEquals(actual1, actual2)
  }

  test("line intersections.should intersect diagonally right angle lines (again)") {
    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((0d, 0d), (5d, 5d)),
      LineSegment((0d, 5d), (5d, 0d))
    )

    val expected: IntersectionVertex = IntersectionVertex(2.5f, 2.5f)

    assertEquals(actual, expected)
  }

  test("line intersections.Intersection use case A") {

    val actual: IntersectionResult = LineSegment.intersection(
      LineSegment((0.0, 0.0), (5.0, 5.0)),
      LineSegment((0.0, 3.0), (5.0, 3.0))
    )

    val expected: IntersectionVertex = IntersectionVertex(3d, 3d)

    assertEquals(actual, expected)

  }

  test("line intersections.Intersection use case B") {
    val lineA = LineSegment((0.0, 0.0), (0.0, 5.0))
    val lineB = LineSegment((0.0, 0.5), (5.0, 3.0))

    val actual: IntersectionResult = LineSegment.intersection(
      lineA,
      lineB
    )

    val expected: IntersectionVertex = IntersectionVertex(0.0, 0.5)

    assertEquals(actual, expected)

    assertEquals(lineA.intersectWithLine(lineB), true)
  }

  test("normals.should calculate the normal for a horizontal line (Left -> Right)") {
    val start: Vertex = Vertex(-10, 1)
    val end: Vertex   = Vertex(10, 1)

    assertEquals(LineSegment.calculateNormal(start, end) === Vector2(0, 1), true)
  }

  test("normals.should calculate the normal for a horizontal line (Right -> Left)") {
    val start: Vertex = Vertex(5, 2)
    val end: Vertex   = Vertex(-5, 2)

    assertEquals(LineSegment.calculateNormal(start, end) === Vector2(0, -1), true)
  }

  test("normals.should calculate the normal for a vertical line (Top -> Bottom") {
    val start: Vertex = Vertex(-1, 10)
    val end: Vertex   = Vertex(-1, -10)

    assertEquals(LineSegment.calculateNormal(start, end) === Vector2(1, 0), true)
  }

  test("normals.should calculate the normal for a vertical line (Bottom -> Top") {
    val start: Vertex = Vertex(1, -10)
    val end: Vertex   = Vertex(1, 10)

    assertEquals(LineSegment.calculateNormal(start, end) === Vector2(-1, 0), true)
  }

  test("normals.should calculate the normal for a diagonal line") {
    val start: Vertex = Vertex(2, 2)
    val end: Vertex   = Vertex(-2, -2)

    assertEquals(LineSegment.calculateNormal(start, end) === Vector2(1, -1), true)
  }

  test("Normalising a point.should be able to normalise a point.10, 10") {
    assertEquals(LineSegment.normaliseVertex(Vector2(10, 10)) === Vector2(1, 1), true)
  }

  test("Normalising a point.should be able to normalise a point.-10, -10") {
    assertEquals(LineSegment.normaliseVertex(Vector2(-10, -10)) === Vector2(-1, -1), true)
  }

  test("Normalising a point.should be able to normalise a point.10, 0") {
    assertEquals(LineSegment.normaliseVertex(Vector2(10, 0)) === Vector2(1, 0), true)
  }

  test("Normalising a point.should be able to normalise a point.0, 10") {
    assertEquals(LineSegment.normaliseVertex(Vector2(0, 10)) === Vector2(0, 1), true)
  }

  test("Normalising a point.should be able to normalise a point.-50, 1000") {
    assertEquals(LineSegment.normaliseVertex(Vector2(-50, 1000)) === Vector2(-1, 1), true)
  }

  test("Vertexs & Lines.Facing a point.facing") {
    val line: LineSegment = LineSegment((1d, 5d), (9d, 5d))
    val point: Vertex     = Vertex(5, 20)

    assertEquals(line.isFacingVertex(point), true)
  }

  test("Vertexs & Lines.Facing a point.not facing") {
    val line: LineSegment = LineSegment((1d, 5d), (9d, 5d))
    val point: Vertex     = Vertex(5, 2)

    assertEquals(line.isFacingVertex(point), false)
  }

  //TODO: Can do a property based check here. Forall points on a line
  // (i.e. start point * slope m < end point)
  test("Vertex on a line.should be able to check if a point is on a line.horizontal") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 10d))
    val point: Vertex     = Vertex(15, 10)

    assertEquals(LineSegment.lineContainsVertex(line, point), true)
  }

  test("Vertex on a line.should be able to check if a point is on a line.vertical") {
    val line: LineSegment = LineSegment((10d, 10d), (10d, 20d))
    val point: Vertex     = Vertex(10, 15)

    assertEquals(LineSegment.lineContainsVertex(line, point), true)
  }

  test("Vertex on a line.should be able to check if a point is on a line.diagonal") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))
    val point: Vertex     = Vertex(15, 15)

    assertEquals(LineSegment.lineContainsVertex(line, point), true)
  }

  test("Vertex on a line.should be able to check if a point is NOT on a line") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))
    val point: Vertex     = Vertex(1, 5)

    assertEquals(LineSegment.lineContainsVertex(line, point), false)
  }

}
