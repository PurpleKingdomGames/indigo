package indigoexts.quadtrees

import indigo.gameengine.scenegraph.datatypes.Point
import org.scalatest.{FunSpec, Matchers}
import indigoexts.grid.GridPoint
import indigoexts.line.LineSegment

class QuadBoundsSpec extends FunSpec with Matchers {

  describe("QuadBounds") {

    it("should be able to check a point is within the bounds") {

      val b = QuadBounds(0, 0, 10, 10)

      b.isPointWithinBounds(GridPoint(5, 5)) shouldEqual true
      b.isPointWithinBounds(GridPoint(0, 0)) shouldEqual true
      b.isPointWithinBounds(GridPoint(-1, 5)) shouldEqual false
      b.isPointWithinBounds(GridPoint(5, 20)) shouldEqual false

    }

    it("should be able to subdivide") {

      val b = QuadBounds(0, 0, 10, 10)

      b.subdivide._1 === QuadBounds(0, 0, 5, 5) shouldEqual true
      b.subdivide._2 === QuadBounds(5, 0, 5, 5) shouldEqual true
      b.subdivide._3 === QuadBounds(0, 5, 5, 5) shouldEqual true
      b.subdivide._4 === QuadBounds(5, 5, 5, 5) shouldEqual true

    }

    it("should be able to re-combine") {
      val original = QuadBounds(0, 0, 0, 2)

      val divisions = original.subdivide

      val recombined =
        QuadBounds.combine(divisions._1, List(divisions._2, divisions._3, divisions._4))

      recombined === original shouldEqual true
    }

  }

  describe("Ray (LineSegment) collision") {

    it("should be able to report a ray collision") {
      val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
      val line: LineSegment  = LineSegment(0, 15, 50, 15)

      QuadBounds.rayCollisionCheck(bounds, line) shouldEqual true
    }

    it("should correctly report when there is no collision") {
      val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
      val line: LineSegment  = LineSegment(0, 15, 5, 15)

      QuadBounds.rayCollisionCheck(bounds, line) shouldEqual false
    }

    it("should report where a line segment passed through the bounds") {
      val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
      val line: LineSegment  = LineSegment(0, 15, 50, 15)

      QuadBounds.rayCollisionPosition(bounds, line) shouldEqual Some(Point(10, 15))
    }

    it("should report where a line segment did not pass through the bounds") {
      val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
      val line: LineSegment  = LineSegment(0, 15, 5, 15)

      QuadBounds.rayCollisionPosition(bounds, line) shouldEqual None
    }

  }
}
