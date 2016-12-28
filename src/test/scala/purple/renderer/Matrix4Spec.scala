package purple.renderer

import org.scalatest.{FunSpec, Matchers}

class Matrix4Spec extends FunSpec with Matchers {

  describe("Translation") {

    /*
      val expected: List[Double] = List(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
      )
     */

    it("should be able to translate in the X direction") {

      val expected: List[Double] = List(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        2, 0, 0, 1
      )

      Matrix4.identity.translate(2.0, 0, 0).mat shouldEqual expected

    }

  }

}

