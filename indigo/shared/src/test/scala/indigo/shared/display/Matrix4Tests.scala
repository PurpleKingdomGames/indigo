package indigo.shared.display

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Matrix4

import utest._

object Matrix4Tests extends TestSuite {

  val tests: Tests =
    Tests {

      "Identity" - {

        val expected: List[Double] = List(
          1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1
        )

        Matrix4.identity.mat === expected ==> true

      }

      "Translation" - {

        "should be able to translate in the X direction" - {

          val expected: List[Double] = List(
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 0, 1
          )

          Matrix4.identity.translate(2.0, 0, 0).mat === expected ==> true

        }

        "should be able to translate in the Y direction" - {

          val expected: List[Double] = List(
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 2, 0, 1
          )

          Matrix4.identity.translate(0, 2.0, 0).mat === expected ==> true

        }

        "should be able to translate in the Z direction" - {

          val expected: List[Double] = List(
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 2, 1
          )

          Matrix4.identity.translate(0, 0, 2.0).mat === expected ==> true

        }

      }

      "Rotation" - {

        val s = Math.sin(Math.PI)
        val c = Math.cos(Math.PI)

        val expected: List[Double] = List(
          c,
          s,
          0,
          0,
          -s,
          c,
          0,
          0,
          0,
          0,
          1,
          0,
          0,
          0,
          0,
          1
        )

        Matrix4.identity.rotate(Math.PI).mat === expected ==> true
      }

      "Scale" - {

        "should be able to translate in the X direction" - {

          val expected: List[Double] = List(
            2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 1
          )

          Matrix4.identity.scale(2.0, 3.0, 4.0).mat === expected ==> true

        }
      }
    }

}
