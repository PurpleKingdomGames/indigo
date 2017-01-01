package purple.renderer

import org.scalatest.{FunSpec, Matchers}

class Vector2Spec extends FunSpec with Matchers {

  val imageSize = Vector2(192, 64)
  val frameSize = Vector2(64, 64)
  val framePosition = Vector2(64, 0)

  describe("Basic vector operation") {

    it("should be able to divide") {
      areDoubleVectorsEqual(Vector2(0.33, 1), Vector2.divide(frameSize, imageSize)) should be(true)
    }

    it("should be able to find a frame") {

      val scaleFactor = Vector2.divide(frameSize, imageSize)

      val translation1 = Vector2.divide(framePosition, frameSize)
      val translation2 = scaleFactor.scale(translation1)

      val result =
        Vector2(1, 1)
          .scale(scaleFactor)
          .translate(translation2)

      areDoubleVectorsEqual(Vector2(0.66, 1), result) should be(true)

    }

  }

  def areDoubleVectorsEqual(expected: Vector2, actual: Vector2): Boolean = {
    areDoublesEqual(expected.x, actual.x) && areDoublesEqual(expected.y, actual.y)
  }

  def areDoublesEqual(expected: Double, actual: Double): Boolean = {
    actual >= expected - 0.01d && actual <= expected + 0.01d
  }

}
