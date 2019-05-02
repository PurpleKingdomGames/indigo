package indigoexts.geometry

import indigo.shared.datatypes.Point
import indigoexts.geometry.IntersectionResult._
import indigoexts.geometry.LineProperties._
import indigo.shared.EqualTo._

import utest._

object LineSegmentTests extends TestSuite {

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */

  val tests: Tests =
    Tests {
      "calculating line components" - {

        "should correctly calculate the line components" - {

          "(0, 0) -> (2, 2)" - {
            val expected: LineComponents = LineComponents(1, 0)

            LineSegment.calculateLineComponents(Point(0, 0), Point(2, 2)) ==> expected
          }

          "(2, 1) -> (3, 4)" - {
            val expected: LineComponents = LineComponents(3, -5)

            LineSegment.calculateLineComponents(Point(2, 1), Point(3, 4)) ==> expected
          }

          "(2, 2) -> (2, -3)" - {
            // Does not work because you can't have m of 0 or b of infinity)
            // i.e. lines parallel to x or y axis
            // We're also getting a divide by 0 because ... / x - x = 0
            val expected = ParallelToAxisY

            LineSegment.calculateLineComponents(Point(2, 2), Point(2, -3)) ==> expected
          }

        }

        "should correctly identify a line parallel to the x-axis" - {
          //m = 0
          LineSegment.calculateLineComponents(Point(-1, 2), Point(1, 2)) ==> ParallelToAxisX
        }

        "should correctly identify a line parallel to the y-axis" - {
          //b = Infinity (or -Infinity)
          LineSegment.calculateLineComponents(Point(1, 2), Point(1, -3)) ==> ParallelToAxisY
        }

      }

      "line intersections" - {

        "should not intersect with a parallel lines" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((-3, 3), (2, 3)),
            LineSegment((1, 1), (-2, 1))
          )

          val expected = NoIntersection

          actual ==> expected
        }

        "should intersect lines at right angles to each other" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((2, 2), (2, -3)),
            LineSegment((-1, -2), (3, -2))
          )

          val expected: IntersectionPoint = IntersectionPoint(2, -2)

          actual ==> expected
        }

        "should intersect diagonally right angle lines" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((1, 1), (5, 5)),
            LineSegment((1, 5), (4, 2))
          )

          val expected: IntersectionPoint = IntersectionPoint(3, 3)

          actual ==> expected
        }

        "should intersect diagonally non-right angle lines" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((1, 5), (3, 1)),
            LineSegment((1, 2), (4, 5))
          )

          val expected: IntersectionPoint = IntersectionPoint(2, 3)

          actual ==> expected
        }

        "should intersect where one line is parallel to the y-axis" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((4, 1), (4, 4)),
            LineSegment((2, 1), (5, 4))
          )

          val expected: IntersectionPoint = IntersectionPoint(4, 3)

          actual ==> expected
        }

        "should intersect where one line is parallel to the x-axis" - {
          val actual: IntersectionResult = LineSegment.intersection(
            LineSegment((1, 2), (5, 2)),
            LineSegment((2, 4), (5, 1))
          )

          val expected: IntersectionPoint = IntersectionPoint(4, 2)

          actual ==> expected
        }

        "should give the same intersection regardless of order" - {
          val actual1: IntersectionResult = LineSegment.intersection(
            LineSegment((0, 15), (50, 15)),
            LineSegment((10, 10), (10, 30))
          )

          val actual2: IntersectionResult = LineSegment.intersection(
            LineSegment((0, 15), (50, 15)),
            LineSegment((10, 10), (10, 30))
          )

          val expected: IntersectionPoint = IntersectionPoint(10, 15)

          actual1 ==> expected
          actual2 ==> expected
          actual1 ==> actual2
        }

      }

      "normals" - {
        "should calculate the normal for a horizontal line (Left -> Right)" - {
          val start: Point = Point(-10, 1)
          val end: Point   = Point(10, 1)

          LineSegment.calculateNormal(start, end) === Point(0, 1) ==> true
        }

        "should calculate the normal for a horizontal line (Right -> Left)" - {
          val start: Point = Point(5, 2)
          val end: Point   = Point(-5, 2)

          LineSegment.calculateNormal(start, end) === Point(0, -1) ==> true
        }

        "should calculate the normal for a vertical line (Top -> Bottom" - {
          val start: Point = Point(-1, 10)
          val end: Point   = Point(-1, -10)

          LineSegment.calculateNormal(start, end) === Point(1, 0) ==> true
        }

        "should calculate the normal for a vertical line (Bottom -> Top" - {
          val start: Point = Point(1, -10)
          val end: Point   = Point(1, 10)

          LineSegment.calculateNormal(start, end) === Point(-1, 0) ==> true
        }

        "should calculate the normal for a diagonal line" - {
          val start: Point = Point(2, 2)
          val end: Point   = Point(-2, -2)

          LineSegment.calculateNormal(start, end) === Point(1, -1) ==> true
        }

      }

      "Normalising a point" - {

        "should be able to normalise a point" - {

          "10, 10" - {
            LineSegment.normalisePoint(Point(10, 10)) === Point(1, 1) ==> true
          }

          "-10, -10" - {
            LineSegment.normalisePoint(Point(-10, -10)) === Point(-1, -1) ==> true
          }

          "10, 0" - {
            LineSegment.normalisePoint(Point(10, 0)) === Point(1, 0) ==> true
          }

          "0, 10" - {
            LineSegment.normalisePoint(Point(0, 10)) === Point(0, 1) ==> true
          }

          "-50, 1000" - {
            LineSegment.normalisePoint(Point(-50, 1000)) === Point(-1, 1) ==> true
          }

        }

      }

      "Points & Lines" - {

        "Facing a point" - {

          val line: LineSegment = LineSegment((1, 5), (9, 5))

          "facing" - {
            val point: Point = Point(5, 2)

            line.isFacingPoint(point) ==> true
          }

          "not facing" - {
            val point: Point = Point(5, 20)

            line.isFacingPoint(point) ==> false
          }

        }

        "Point on a line" - {

          //TODO: Can do a property based check here. Forall points on a line
          // (i.e. start point * slope m < end point)
          "should be able to check if a point is on a line" - {
            "horizontal" - {
              val line: LineSegment = LineSegment((10, 10), (20, 10))
              val point: Point      = Point(15, 10)

              LineSegment.lineContainsPoint(line, point) ==> true
            }

            "vertical" - {
              val line: LineSegment = LineSegment((10, 10), (10, 20))
              val point: Point      = Point(10, 15)

              LineSegment.lineContainsPoint(line, point) ==> true
            }

            "diagonal" - {
              val line: LineSegment = LineSegment((10, 10), (20, 20))
              val point: Point      = Point(15, 15)

              LineSegment.lineContainsPoint(line, point) ==> true
            }
          }

          "should be able to check if a point is NOT on a line" - {
            val line: LineSegment = LineSegment((10, 10), (20, 20))
            val point: Point      = Point(1, 5)

            LineSegment.lineContainsPoint(line, point) ==> false
          }

        }
      }
    }

}
