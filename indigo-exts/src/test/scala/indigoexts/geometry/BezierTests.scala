package indigoexts.geometry

import utest._
import indigo.EqualTo._
import indigo.shared.time.Millis
import indigo.shared.datatypes.Rectangle

object BezierTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Interpolation" - {
        Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 0d) === Vertex(0, 0) ==> true
        Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 0.5d) === Vertex(5, 5) ==> true
        Bezier.interpolate(Vertex(0, 0), Vertex(10, 10), 1d) === Vertex(10, 10) ==> true
      }

      "Reduction" - {

        "Empty list" - {
          Bezier.reduce(Nil, 0d) === Vertex.zero ==> true
        }

        "one point" - {
          Bezier.reduce(List(Vertex(1, 1)), 0d) === Vertex(1, 1) ==> true
        }

        "two points" - {
          Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 0d) === Vertex(0, 0) ==> true
          Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 0.5d) === Vertex(5, 5) ==> true
          Bezier.reduce(List(Vertex(0, 0), Vertex(10, 10)), 1d) === Vertex(10, 10) ==> true
        }

        "three points" - {
          Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 0d) === Vertex(0, 0) ==> true
          Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 0.5d) === Vertex(4, 2) ==> true
          Bezier.reduce(List(Vertex(0, 0), Vertex(5, 5), Vertex(10, 0)), 1d) === Vertex(10, 0) ==> true
        }

      }

      "One dimensional (1 point)" - {

        val bezier =
          Bezier(Vertex(5, 5))

        bezier.at(0d) === Vertex(5, 5) ==> true
        bezier.at(0.5d) === Vertex(5, 5) ==> true
        bezier.at(1d) === Vertex(5, 5) ==> true

      }

      "Linear (2 points)" - {

        val bezier =
          Bezier(Vertex(0, 0), Vertex(10, 10))

        bezier.at(-50d) === Vertex(0, 0) ==> true
        bezier.at(0d) === Vertex(0, 0) ==> true
        bezier.at(0.25d) === Vertex(2, 2) ==> true
        bezier.at(0.5d) === Vertex(5, 5) ==> true
        bezier.at(0.75d) === Vertex(7, 7) ==> true
        bezier.at(1d) === Vertex(10, 10) ==> true
        bezier.at(100d) === Vertex(10, 10) ==> true

      }

      "Quadtratic (3 points)" - {

        val bezier =
          Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10))

        /*
          For 0.5d:

          2,2 4,7 20,10
          (2,2 4,7) (4,7 20,10)
          3,4 12,8
          7,6

         */

        bezier.at(0d) === Vertex(2, 2) ==> true
        bezier.at(0.5d) === Vertex(7, 6) ==> true
        bezier.at(1d) === Vertex(20, 10) ==> true

      }

      "Higher-Order (4 or more points)" - {

        val bezier =
          Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

        /*
          For 0.5d:

          2,2 4,7 20,10 3,100
          (2,2 4,7) (4,7 20,10) (20,10 3,100)
          (3,4 12,8) (12,8 [((3-20)/2)+20 = 11],55)
          7,6 11,31

          [((11-7)/2)+7 = 9] , ((31-6)/2)+6 = 18
          0,12

         */

        bezier.at(0d) === Vertex(2, 2) ==> true
        bezier.at(0.5d) === Vertex(9, 18) ==> true
        bezier.at(1d) === Vertex(3, 100) ==> true

      }

      "to points" - {

        "linear" - {
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

          actual === expected ==> true
        }

        "higher order" - {
          val bezier =
            Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

          val actual = bezier.toVertices(2)

          val expected =
            List(
              Vertex(2, 2),
              Vertex(9, 18),
              Vertex(3, 100)
            )

          actual === expected ==> true
        }
      }

      "to polygon" - {

        "linear" - {
          val bezier =
            Bezier(Vertex.zero, Vertex(100, 0))

          val actual: Int = bezier.toPolygon(10).edgeCount

          val expected: Int = 10

          actual ==> expected
        }

      }

      "to line segments" - {
        val bezier =
          Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

        val lineSegments = bezier.toLineSegments(2)

        lineSegments.length ==> 2
        lineSegments(0) === LineSegment(Vertex(2, 2), Vertex(9, 18)) ==> true
        lineSegments(1) === LineSegment(Vertex(9, 18), Vertex(3, 100)) ==> true
      }

      "to signal" - {
        val bezier =
          Bezier(Vertex(2, 2), Vertex(4, 7), Vertex(20, 10), Vertex(3, 100))

        val signal =
          bezier.toSignal(Millis(1500))

        signal.at(Millis(0)) === Vertex(2, 2) ==> true
        signal.at(Millis(750)) === Vertex(9, 18) ==> true
        signal.at(Millis(1500)) === Vertex(3, 100) ==> true
      }

      "give bounding rectangle" - {

        val bezier =
          Bezier(Vertex(20, 10), Vertex(3, 100), Vertex(2, 2))

        val actual =
          bezier.bounds

        val expected: BoundingBox =
          BoundingBox(2, 2, 18, 98)

        actual === expected ==> true
      }

    }

}
