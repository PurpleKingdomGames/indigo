package indigo.shared.datatypes

import indigo.shared.EqualTo._

import utest._

object PointTests extends TestSuite {

  val tests: Tests =
    Tests {
      "Interpolation" - {

        "should be able to calculate a linear interpolation" - {

          Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 5) === Point(15, 15) ==> true
          Point.linearInterpolation(Point(10, 20), Point(20, 20), 10, 5) === Point(15, 20) ==> true
          Point.linearInterpolation(Point(20, 10), Point(20, 20), 10, 5) === Point(20, 15) ==> true
          Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 1) === Point(11, 11) ==> true
          Point.linearInterpolation(Point(10, 10), Point(-10, -10), 100, 99) === Point(-9, -9) ==> true

        }

      }

      "calculating distance" - {

        "should be able to calculate the distance between horizontal points" - {
          val p1: Point = Point(10, 10)
          val p2: Point = Point(30, 10)

          p1.distanceTo(p2) ==> 20
        }

        "should be able to calculate the distance between vertical points" - {
          val p1: Point = Point(10, 10)
          val p2: Point = Point(10, 30)

          p1.distanceTo(p2) ==> 20
        }

        "should be able to calculate the distance between diagonal points" - {
          val p1: Point = Point(10, 10)
          val p2: Point = Point(30, 30)

          p1.distanceTo(p2) ==> Math.sqrt((20d * 20d) * (20d * 20d))
        }

      }
    }
}
