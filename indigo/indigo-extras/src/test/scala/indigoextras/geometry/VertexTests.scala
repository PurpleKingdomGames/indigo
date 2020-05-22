package indigoextras.geometry

import utest._

import indigo.shared.EqualTo._

object VertexTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Distance" - {

        "horizontal distance" - {
          Vertex.zero.distanceTo(Vertex(10, 0)) ==> 10d
        }

        "vertical distance" - {
          Vertex.zero.distanceTo(Vertex(0, 0.5)) ==> 0.5d
          Vertex(0, 0.1).distanceTo(Vertex(0, 0.5)) ==> 0.4d
        }

        "diagonal distance" - {
          val a = (0.9d - 0.1d) * 0.9d - 0.1d
          val b = (0.9d - 0.1d) * 0.9d - 0.1d
          val c = Math.sqrt(a + b)

          nearEnoughEqual(Vertex(0.1, 0.1).distanceTo(Vertex(0.9, 0.9)), c, 0.025d) ==> true
        }

        "diagonal distance > 1" - {
          val a = Math.pow(100.0d, 2)
          val b = Math.pow(100.0d, 2)
          val c = Math.sqrt(a + b)

          nearEnoughEqual(Vertex(0.0, 0.0).distanceTo(Vertex(100.0, 100.0)), c, 0.025d) ==> true
        }

      }

    }

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

}
