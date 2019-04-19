package indigo.renderer

import indigo.gameengine.display.Vector2
import indigo.shared.EqualTo._

import utest._

object SpriteSheetFrameTests extends TestSuite {

  val tests: Tests =
    Tests {
      "calculating the bounds of a texture within another texture" - {

        "should be able to find the sub-coordinates of a texture" - {

          val imageSize     = Vector2(256, 256)
          val frameSize     = Vector2(64, 64)
          val framePosition = Vector2(64, 0)
          val textureOffset = Vector2(10, 10)

          val offset = SpriteSheetFrame.calculateFrameOffset(imageSize, frameSize, framePosition, textureOffset)

          val textureCoordinate1   = Vector2(0, 0)
          val resultingMultiplier1 = textureCoordinate1.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier1) === Vector2(74, 10) ==> true

          val textureCoordinate2   = Vector2(0.5, 0.5)
          val resultingMultiplier2 = textureCoordinate2.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier2) === Vector2(106, 42) ==> true

          val textureCoordinate3   = Vector2(1, 1)
          val resultingMultiplier3 = textureCoordinate3.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier3) === Vector2(138, 74.0) ==> true

        }

        "should find the right coordinates of the frame when multiplied out by a texture coordinate" - {

          val imageSize     = Vector2(192, 64)
          val frameSize     = Vector2(64, 64)
          val framePosition = Vector2(64, 0)
          val textureOffset = Vector2.zero

          val offset = SpriteSheetFrame.calculateFrameOffset(imageSize, frameSize, framePosition, textureOffset)

          val textureCoordinate1   = Vector2(0, 0)
          val resultingMultiplier1 = textureCoordinate1.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier1) === Vector2(64, 0) ==> true

          val textureCoordinate2   = Vector2(0.5, 0.5)
          val resultingMultiplier2 = textureCoordinate2.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier2) === Vector2(96, 32) ==> true

          val textureCoordinate3   = Vector2(1, 1)
          val resultingMultiplier3 = textureCoordinate3.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(imageSize, resultingMultiplier3) === Vector2(128.0, 64.0) ==> true

        }

      }
    }

}
