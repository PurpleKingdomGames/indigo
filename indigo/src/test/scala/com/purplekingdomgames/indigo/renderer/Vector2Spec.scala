package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class Vector2Spec extends FunSpec with Matchers {

  val imageSize     = Vector2(192, 64)
  val frameSize     = Vector2(64, 64)
  val framePosition = Vector2(64, 0)

  describe("Basic vector operation") {

    it("should be able to divide") {
      areDoubleVectorsEqual(Vector2(0.33, 1), frameSize / imageSize) should be(true)
    }

    it("should be able to find a frame") {

      val scaleFactor = frameSize / imageSize

      val frameOffsetFactor = framePosition / frameSize
      val multiplier        = scaleFactor * frameOffsetFactor

      val result =
        Vector2(1, 1)
          .scale(scaleFactor)
          .translate(multiplier)

      areDoubleVectorsEqual(Vector2(0.66, 1), result) should be(true)

    }

    it("should be able to scale and translate") {

      val res = Vector2(10, 10).scale(Vector2(2, 2)).translate(Vector2(5, 5))

      res.x shouldEqual 25
      res.y shouldEqual 25

    }

  }

  def areDoubleVectorsEqual(expected: Vector2, actual: Vector2): Boolean =
    areDoublesEqual(expected.x, actual.x) && areDoublesEqual(expected.y, actual.y)

  def areDoublesEqual(expected: Double, actual: Double): Boolean =
    actual >= expected - 0.01d && actual <= expected + 0.01d

}
