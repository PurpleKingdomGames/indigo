package com.purplekingdomgames.indigo.renderer

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

      val frameOffsetFactor = Vector2.divide(framePosition, frameSize)
      val multiplier = scaleFactor.scale(frameOffsetFactor)

      val result =
        Vector2(1, 1)
          .scale(scaleFactor)
          .translate(multiplier)

      areDoubleVectorsEqual(Vector2(0.66, 1), result) should be(true)

    }

    it("should find the right coordinates of the frame when multiplied out by a texture coordinate") {

      val offset = SpriteSheetFrame.calculateFrameOffset(imageSize, frameSize, framePosition)

      val textureCoordinate1 = Vector2(0, 0)
      val resultingMultiplier1 = textureCoordinate1.scale(offset.scale).translate(offset.translate)

      Vector2.multiply(imageSize, resultingMultiplier1) shouldEqual Vector2(64, 0)

      val textureCoordinate2 = Vector2(0.5, 0.5)
      val resultingMultiplier2 = textureCoordinate2.scale(offset.scale).translate(offset.translate)

      Vector2.multiply(imageSize, resultingMultiplier2) shouldEqual Vector2(96, 32)

      val textureCoordinate3 = Vector2(1, 1)
      val resultingMultiplier3 = textureCoordinate3.scale(offset.scale).translate(offset.translate)

      Vector2.multiply(imageSize, resultingMultiplier3) shouldEqual Vector2(128.0, 64.0)

    }

  }

  def areDoubleVectorsEqual(expected: Vector2, actual: Vector2): Boolean = {
    areDoublesEqual(expected.x, actual.x) && areDoublesEqual(expected.y, actual.y)
  }

  def areDoublesEqual(expected: Double, actual: Double): Boolean = {
    actual >= expected - 0.01d && actual <= expected + 0.01d
  }

}
