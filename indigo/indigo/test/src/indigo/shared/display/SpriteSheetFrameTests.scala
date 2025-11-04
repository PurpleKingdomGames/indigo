package indigo.shared.display

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Vector2

class SpriteSheetFrameTests extends munit.FunSuite {

  // calculating the bounds of a texture within another texture

  test("should be able to find the sub-coordinates of a texture") {

    val atlasSize     = Vector2(256, 256)
    val frameCrop     = Rectangle(64, 0, 64, 64)
    val textureOffset = Vector2(10, 10)

    val offset = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

    val textureCoordinate1   = Vector2(0, 0)
    val resultingMultiplier1 = textureCoordinate1.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier1), Vector2(74, 10))

    val textureCoordinate2   = Vector2(0.5, 0.5)
    val resultingMultiplier2 = textureCoordinate2.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier2), Vector2(106, 42))

    val textureCoordinate3   = Vector2(1, 1)
    val resultingMultiplier3 = textureCoordinate3.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier3), Vector2(138, 74.0))

  }

  test("should find the right coordinates of the frame when multiplied out by a texture coordinate") {

    val atlasSize     = Vector2(192, 64)
    val frameCrop     = Rectangle(64, 0, 64, 64)
    val textureOffset = Vector2.zero

    val offset = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

    val textureCoordinate1   = Vector2(0, 0)
    val resultingMultiplier1 = textureCoordinate1.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier1), Vector2(64, 0))

    val textureCoordinate2   = Vector2(0.5, 0.5)
    val resultingMultiplier2 = textureCoordinate2.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier2), Vector2(96, 32))

    val textureCoordinate3   = Vector2(1, 1)
    val resultingMultiplier3 = textureCoordinate3.scaleBy(offset.scale).translate(offset.translate)

    assertEquals(Vector2.multiply(atlasSize, resultingMultiplier3), Vector2(128.0, 64.0))

  }

  test("should be able to calculate other offsets based on this one") {

    val atlasSize     = Vector2(128, 128)
    val frameCrop     = Rectangle(0, 0, 64, 64)
    val textureOffset = Vector2(0, 0)

    val offset0 = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

    assertEquals(offset0.translate, Vector2(0.0, 0.0))
    assertEquals(offset0.scale, Vector2(0.5, 0.5))

    val offset1 = offset0.offsetToCoords(Vector2(64, 0))
    assertEquals(offset1, Vector2(0.5, 0.0))

    val offset2 = offset0.offsetToCoords(Vector2(64, 64))
    assertEquals(offset2, Vector2(0.5, 0.5))

    val offset3 = offset0.offsetToCoords(Vector2(0, 64))
    assertEquals(offset3, Vector2(0.0, 0.5))

  }

  test("should be able to calculate other offsets based on this one, when the image is cropped") {

    val atlasSize     = Vector2(128, 128)
    val frameCrop     = Rectangle(16, 16, 32, 32)
    val textureOffset = Vector2(0, 0)

    val offset0 = SpriteSheetFrame.calculateFrameOffset(atlasSize, frameCrop, textureOffset)

    assertEquals(offset0.translate, Vector2(0.125, 0.125))
    assertEquals(offset0.scale, Vector2(0.25, 0.25))

    val offset1 = offset0.offsetToCoords(Vector2(64, 0))
    assertEquals(offset1, Vector2(0.625, 0.125))

    val offset2 = offset0.offsetToCoords(Vector2(64, 64))
    assertEquals(offset2, Vector2(0.625, 0.625))

    val offset3 = offset0.offsetToCoords(Vector2(0, 64))
    assertEquals(offset3, Vector2(0.125, 0.625))

  }

}
