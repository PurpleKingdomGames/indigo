package indigo.gameengine.display

import utest._

object Matrix4Tests extends TestSuite {

  val tests: Tests =
    Tests {
      "Translation" - {

        /*
      val expected: List[Double] = List(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
      )
         */

        "should be able to translate in the X direction" - {

          val expected: List[Double] = List(
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 0, 1
          )

          Matrix4.identity.translate(2.0, 0, 0).mat ==> expected

        }

      }
    }

}
