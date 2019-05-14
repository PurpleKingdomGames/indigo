package indigoexts.geometry

import utest._
import indigo.shared.datatypes.Point
import indigo.EqualTo._

object BezierTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Interpolation" - {
        Bezier.interpolate(Point(0, 0), Point(10, 10), 0d) === Point(0, 0) ==> true
        Bezier.interpolate(Point(0, 0), Point(10, 10), 0.5d) === Point(5, 5) ==> true
        Bezier.interpolate(Point(0, 0), Point(10, 10), 1d) === Point(10, 10) ==> true
      }

      "Reduction" - {

        "Empty list" - {
          Bezier.reduce(Nil, 0d) === Point.zero ==> true
        }

        "one point" - {
          Bezier.reduce(List(Point(1, 1)), 0d) === Point(1, 1) ==> true
        }

        "two points" - {
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 0d) === Point(0, 0) ==> true
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 0.5d) === Point(5, 5) ==> true
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 1d) === Point(10, 10) ==> true
        }

        "three points" - {
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 0d) === Point(0, 0) ==> true
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 0.5d) === Point(4, 2) ==> true
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 1d) === Point(10, 0) ==> true
        }

      }

      "One dimensional (1 point)" - {

        val bezier =
          Bezier(Point(5, 5))

        bezier.at(0d) === Point(5, 5) ==> true
        bezier.at(0.5d) === Point(5, 5) ==> true
        bezier.at(1d) === Point(5, 5) ==> true

      }

      "Linear (2 points)" - {

        val bezier =
          Bezier(Point(0, 0), Point(10, 10))

        bezier.at(-50d) === Point(0, 0) ==> true
        bezier.at(0d) === Point(0, 0) ==> true
        bezier.at(0.25d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(5, 5) ==> true
        bezier.at(0.75d) === Point(7, 7) ==> true
        bezier.at(1d) === Point(10, 10) ==> true
        bezier.at(100d) === Point(10, 10) ==> true

      }

      "Quadtratic (3 points)" - {

        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10))

        /*
          For 0.5d:

          2,2 4,7 20,10
          (2,2 4,7) (4,7 20,10)
          3,4 12,8
          7,6

         */

        bezier.at(0d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(7, 6) ==> true
        bezier.at(1d) === Point(20, 10) ==> true

      }

      "Higher-Order (4 or more points)" - {

        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10), Point(3, 100))

        /*
          For 0.5d:

          2,2 4,7 20,10 3,100
          (2,2 4,7) (4,7 20,10) (20,10 3,100)
          (3,4 12,8) (12,8 [((3-20)/2)+20 = 11],55)
          7,6 11,31

          [((11-7)/2)+7 = 9] , ((31-6)/2)+6 = 18
          0,12

         */

        bezier.at(0d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(9, 18) ==> true
        bezier.at(1d) === Point(3, 100) ==> true

      }

    }

}
