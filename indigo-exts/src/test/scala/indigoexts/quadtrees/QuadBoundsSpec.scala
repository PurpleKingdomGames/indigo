package indigoexts.quadtrees

import utest._

import indigo.gameengine.scenegraph.datatypes.Point
import indigoexts.grid.GridPoint
import indigoexts.line.LineSegment
import indigo.shared.EqualTo._

object QuadBoundsTests extends TestSuite {

  val tests: Tests =
    Tests {
      "QuadBounds" - {

        "should be able to check a point is within the bounds" - {

          val b = QuadBounds(0, 0, 10, 10)

          b.isPointWithinBounds(GridPoint(5, 5)) ==> true
          b.isPointWithinBounds(GridPoint(0, 0)) ==> true
          b.isPointWithinBounds(GridPoint(-1, 5)) ==> false
          b.isPointWithinBounds(GridPoint(5, 20)) ==> false

        }

        "should be able to subdivide" - {

          val b = QuadBounds(0, 0, 10, 10)

          b.subdivide._1 === QuadBounds(0, 0, 5, 5) ==> true
          b.subdivide._2 === QuadBounds(5, 0, 5, 5) ==> true
          b.subdivide._3 === QuadBounds(0, 5, 5, 5) ==> true
          b.subdivide._4 === QuadBounds(5, 5, 5, 5) ==> true

        }

        "should be able to re-combine" - {
          val original = QuadBounds(0, 0, 0, 2)

          val divisions = original.subdivide

          val recombined =
            QuadBounds.combine(divisions._1, List(divisions._2, divisions._3, divisions._4))

          recombined === original ==> true
        }

      }

      "Ray (LineSegment) collision" - {

        "should be able to report a ray collision" - {
          val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
          val line: LineSegment  = LineSegment(0, 15, 50, 15)

          QuadBounds.rayCollisionCheck(bounds, line) ==> true
        }

        "should correctly report when there is no collision" - {
          val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
          val line: LineSegment  = LineSegment(0, 15, 5, 15)

          QuadBounds.rayCollisionCheck(bounds, line) ==> false
        }

        "should report where a line segment passed through the bounds" - {
          val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
          val line: LineSegment  = LineSegment(0, 15, 50, 15)

          QuadBounds.rayCollisionPosition(bounds, line) ==> Some(Point(10, 15))
        }

        "should report where a line segment did not pass through the bounds" - {
          val bounds: QuadBounds = QuadBounds(10, 10, 20, 20)
          val line: LineSegment  = LineSegment(0, 15, 5, 15)

          QuadBounds.rayCollisionPosition(bounds, line) ==> None
        }

      }
    }
}
