package indigo.shared.display

import utest._

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Rectangle

object SpriteSheetFrameTests extends TestSuite {

  val tests: Tests =
    Tests {
      "calculating the bounds of a texture within another texture" - {

        "should be able to find the sub-coordinates of a texture" - {

          val atlasSize     = Vector2(256, 256)
          val frameCrop     = Rectangle(64, 0, 64, 64)
          val textureOffset = Vector2(10, 10)

          val offset = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

          val textureCoordinate1   = Vector2(0, 0)
          val resultingMultiplier1 = textureCoordinate1.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier1) === Vector2(74, 10) ==> true

          val textureCoordinate2   = Vector2(0.5, 0.5)
          val resultingMultiplier2 = textureCoordinate2.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier2) === Vector2(106, 42) ==> true

          val textureCoordinate3   = Vector2(1, 1)
          val resultingMultiplier3 = textureCoordinate3.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier3) === Vector2(138, 74.0) ==> true

        }

        "should find the right coordinates of the frame when multiplied out by a texture coordinate" - {

          val atlasSize     = Vector2(192, 64)
          val frameCrop     = Rectangle(64, 0, 64, 64)
          val textureOffset = Vector2.zero

          val offset = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

          val textureCoordinate1   = Vector2(0, 0)
          val resultingMultiplier1 = textureCoordinate1.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier1) === Vector2(64, 0) ==> true

          val textureCoordinate2   = Vector2(0.5, 0.5)
          val resultingMultiplier2 = textureCoordinate2.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier2) === Vector2(96, 32) ==> true

          val textureCoordinate3   = Vector2(1, 1)
          val resultingMultiplier3 = textureCoordinate3.scale(offset.scale).translate(offset.translate)

          Vector2.multiply(atlasSize, resultingMultiplier3) === Vector2(128.0, 64.0) ==> true

        }

        "should be able to calculate other offsets based on this one" - {

          val atlasSize     = Vector2(128, 128)
          val frameCrop     = Rectangle(0, 0, 64, 64)
          val textureOffset = Vector2(0, 0)

          val offset0 = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

          offset0.translate ==> Vector2(0.0, 0.0)
          offset0.scale ==> Vector2(0.5, 0.5)

          val offset1 = offset0.offsetToCoords(Vector2(64, 0))
          offset1 ==> Vector2(0.5, 0.0)

          val offset2 = offset0.offsetToCoords(Vector2(64, 64))
          offset2 ==> Vector2(0.5, 0.5)

          val offset3 = offset0.offsetToCoords(Vector2(0, 64))
          offset3 ==> Vector2(0.0, 0.5)

        }

        "should be able to calculate other offsets based on this one, when the image is cropped" - {

          val atlasSize     = Vector2(128, 128)
          val frameCrop     = Rectangle(16, 16, 32, 32)
          val textureOffset = Vector2(0, 0)

          val offset0 = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

          offset0.translate ==> Vector2(0.125, 0.125)
          offset0.scale ==> Vector2(0.25, 0.25)

          val offset1 = offset0.offsetToCoords(Vector2(64, 0))
          offset1 ==> Vector2(0.625, 0.125)

          val offset2 = offset0.offsetToCoords(Vector2(64, 64))
          offset2 ==> Vector2(0.625, 0.625)

          val offset3 = offset0.offsetToCoords(Vector2(0, 64))
          offset3 ==> Vector2(0.125, 0.625)

        }

      }
    }

}
