package snake.model.quadtrees

import indigo.shared.datatypes.Point
import snake.model.grid.GridPoint
import indigoextras.geometry.LineSegment

class QuadBoundsTests extends munit.FunSuite {

  test("QuadBounds.should be able to check a point is within the bounds") {

    val b = QuadBounds(0, 0, 10, 10)

    assertEquals(b.isPointWithinBounds(GridPoint(5, 5)), true)
    assertEquals(b.isPointWithinBounds(GridPoint(0, 0)), true)
    assertEquals(b.isPointWithinBounds(GridPoint(-1, 5)), false)
    assertEquals(b.isPointWithinBounds(GridPoint(5, 20)), false)

  }

  test("QuadBounds.should be able to subdivide") {

    val b = QuadBounds(0, 0, 10, 10)

    assertEquals(b.subdivide._1 === QuadBounds(0, 0, 5, 5), true)
    assertEquals(b.subdivide._2 === QuadBounds(5, 0, 5, 5), true)
    assertEquals(b.subdivide._3 === QuadBounds(0, 5, 5, 5), true)
    assertEquals(b.subdivide._4 === QuadBounds(5, 5, 5, 5), true)

  }

  test("QuadBounds.should be able to re-combine") {
    val original = QuadBounds(0, 0, 0, 2)

    val divisions = original.subdivide

    val recombined =
      QuadBounds.combine(divisions._1, List(divisions._2, divisions._3, divisions._4))

    assertEquals(recombined === original, true)
  }

  test("Ray (LineSegment) collision.should be able to report a ray collision") {
    val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
    val line: LineSegment  = LineSegment(0, 15, 50, 15)

    assertEquals(QuadBounds.rayCollisionCheck(bounds, line), true)
  }

  test("Ray (LineSegment) collision.should correctly report when there is no collision") {
    val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
    val line: LineSegment  = LineSegment(0, 15, 5, 15)

    assertEquals(QuadBounds.rayCollisionCheck(bounds, line), false)
  }

  test("Ray (LineSegment) collision.should report where a line segment passed through the bounds") {
    val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
    val line: LineSegment  = LineSegment(0, 15, 50, 15)

    assertEquals(QuadBounds.rayCollisionPosition(bounds, line) == Some(Point(10, 15)), true)
  }

  test("Ray (LineSegment) collision.should report where a line segment did not pass through the bounds") {
    val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
    val line: LineSegment  = LineSegment(0, 15, 5, 15)

    assertEquals(QuadBounds.rayCollisionPosition(bounds, line), None)
  }
}
