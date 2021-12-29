package indigoextras.geometry

import indigo.shared.datatypes.Vector2

class LineSegmentTests extends munit.FunSuite {

  test("approx equal") {
    assert(LineSegment(5.0, 5.0, 5.0, 5.0) ~== LineSegment(4.999999, 5.00001, 4.999999, 5.00001))
    assert(!(LineSegment(5.0, 5.0, 5.0, 5.0) ~== LineSegment(4.98, 5.00001, 4.98, 5.00001)))
  }

  test("length") {
    assertEquals(LineSegment(Vertex(10, 10), Vertex(20, 10)).length, 10.0d)
    assertEquals(LineSegment(Vertex(10, 10), Vertex(10, -10)).length, 20.0d)
    assertEquals(Math.floor(LineSegment(Vertex(10, 10), Vertex(15, 13)).length * 100) * 0.01, 5.83d)
  }

  test("moving the whole line segment") {
    val actual =
      LineSegment(Vertex(10, 10), Vertex(20, -30)).moveTo(-5, 50)

    val expected =
      LineSegment(Vertex(-5, 50), Vertex(5, 10))

    assertEquals(actual, expected)
  }

  test("normals.should calculate the normal for a horizontal line (Left -> Right)") {
    val start: Vertex = Vertex(-10, 1)
    val end: Vertex   = Vertex(10, 1)
    val line          = LineSegment(start, end)

    assert(clue(line.normal) ~== clue(Vector2(0, 1)))
  }

  test("normals.should calculate the normal for a horizontal line (Right -> Left)") {
    val start: Vertex = Vertex(5, 2)
    val end: Vertex   = Vertex(-5, 2)
    val line          = LineSegment(start, end)

    assert(clue(line.normal) ~== clue(Vector2(0, -1)))
  }

  test("normals.should calculate the normal for a vertical line (Top -> Bottom") {
    val start: Vertex = Vertex(-1, 10)
    val end: Vertex   = Vertex(-1, -10)
    val line          = LineSegment(start, end)

    assert(clue(line.normal) ~== clue(Vector2(1, 0)))
  }

  test("normals.should calculate the normal for a vertical line (Bottom -> Top") {
    val start: Vertex = Vertex(1, -10)
    val end: Vertex   = Vertex(1, 10)
    val line          = LineSegment(start, end)

    assert(clue(line.normal) ~== clue(Vector2(-1, 0)))
  }

  test("normals.should calculate the normal for a diagonal line") {
    val start: Vertex = Vertex(2, 2)
    val end: Vertex   = Vertex(-2, -2)
    val line          = LineSegment(start, end)

    assert(clue(line.normal) ~== clue(Vector2(0.7071, -0.7071)))
  }

  test("Vertexs & Lines.Facing a vertex.facing") {
    val line: LineSegment = LineSegment((1d, 5d), (9d, 5d))
    val vertex: Vertex    = Vertex(5, 20)

    assert(line.isFacingVertex(vertex))
  }

  test("Vertexs & Lines.Facing a vertex.not facing") {
    val line: LineSegment = LineSegment((1d, 5d), (9d, 5d))
    val vertex: Vertex    = Vertex(5, 2)

    assert(!line.isFacingVertex(vertex))
  }

  // Could do a property based check here? Forall vertices on a line
  // (i.e. start vertex * slope m < end vertex)
  test("Vertex on a line.should be able to check if a vertex is on a line.horizontal") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 10d))
    val vertex: Vertex    = Vertex(15, 10)

    assert(line.contains(vertex))
  }

  test("Vertex on a line.should be able to check if a vertex is on a line.vertical") {
    val line: LineSegment = LineSegment((10d, 10d), (10d, 20d))
    val vertex: Vertex    = Vertex(10, 15)

    assert(line.contains(vertex))
  }

  test("Vertex on a line.should be able to check if a vertex is on a line.diagonal") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))
    val vertex: Vertex    = Vertex(15, 15)

    assert(line.contains(vertex))
  }

  test("Vertex on a line.should be able to check if a vertex is NOT on a line") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))
    val vertex: Vertex    = Vertex(1, 5)

    assert(!line.contains(vertex))
  }

  test("moveTo | moveBy | moveStartTo | moveStartBy | moveEndTo | moveEndBy") {

    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))

    assertEquals(line.moveTo(1, 2), LineSegment((1d, 2d), (11d, 12d)))
    assertEquals(line.moveBy(1, 2), LineSegment((11d, 12d), (21d, 22d)))

    assertEquals(line.moveStartTo(1, 2), LineSegment((1d, 2d), (20d, 20d)))
    assertEquals(line.moveStartBy(1, 2), LineSegment((11d, 12d), (20d, 20d)))

    assertEquals(line.moveEndTo(1, 2), LineSegment((10d, 10d), (1d, 2d)))
    assertEquals(line.moveEndBy(1, 2), LineSegment((10d, 10d), (21d, 22d)))
  }

  test("invert | flip") {
    assertEquals(LineSegment((10d, 10d), (20d, 20d)).invert, LineSegment((20d, 20d), (10d, 10d)))
    assertEquals(LineSegment((10d, 10d), (20d, 20d)).flip, LineSegment((20d, 20d), (10d, 10d)))
  }

  test("Finding the closet vertex on the line to a vertex") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))

    // before line
    assertEquals(line.closestPointOnLine(Vertex(1, 1)), Some(line.start))
    assertEquals(line.closestPointOnLine(Vertex(10, 10)), Some(line.start))

    // some where in the middle
    assertEquals(line.closestPointOnLine(Vertex(15, 10)), Some(Vertex(12.5, 12.5)))
    assertEquals(line.closestPointOnLine(Vertex(15, 15)), Some(Vertex(15, 15)))
    assertEquals(line.closestPointOnLine(Vertex(15, 20)), Some(Vertex(17.5, 17.5)))

    // past line
    assertEquals(line.closestPointOnLine(Vertex(20, 20)), Some(line.end))
    assertEquals(line.closestPointOnLine(Vertex(27, 21)), Some(line.end))
  }

  test("signed distance function") {
    val line: LineSegment = LineSegment((10d, 10d), (20d, 20d))

    // start
    assertEquals(line.sdf(Vertex(0, 10)), 10.0d)

    assertEquals(line.sdf(Vertex(15, 10)), Vertex(15, 10).distanceTo(Vertex(12.5, 12.5)))
    assertEquals(line.sdf(Vertex(15, 15)), 0.0d)
    assertEquals(line.sdf(Vertex(15, 20)), Vertex(15, 20).distanceTo(Vertex(17.5, 17.5)))

    // end
    assertEquals(line.sdf(Vertex(25, 25)), Math.sqrt(5 * 5 + 5 * 5))
  }

  test("line intersections.Intersection use case A") {

    val lineA = LineSegment((0.0, 0.0), (5.0, 5.0))
    val lineB = LineSegment((0.0, 3.0), (5.0, 3.0))

    assert(lineA.intersectsWith(lineB))
    assertEquals(lineA.intersectsAt(lineB), Some(Vertex(3d, 3d)))

  }

  test("line intersections.Intersection use case B") {
    val lineA = LineSegment((0.0, 0.0), (0.0, 5.0))
    val lineB = LineSegment((0.0, 0.5), (5.0, 3.0))

    assert(lineA.intersectsWith(lineB))
    assertEquals(lineA.intersectsAt(lineB), Some(Vertex(0.0, 0.5)))
  }

}
