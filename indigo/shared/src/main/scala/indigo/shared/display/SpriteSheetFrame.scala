package indigo.shared.display

import indigo.shared.datatypes.Vector2

import indigo.shared.{AsString, EqualTo}

object SpriteSheetFrame {

  def calculateFrameOffset(imageSize: Vector2, frameSize: Vector2, framePosition: Vector2, textureOffset: Vector2): SpriteSheetFrameCoordinateOffsets = {
    val scaleFactor       = frameSize / imageSize
    val frameOffsetFactor = (framePosition + textureOffset) / frameSize
    val translationFactor = scaleFactor * frameOffsetFactor

    val f: Vector2 => Vector2 =
      v => scaleFactor * ((v + textureOffset) / frameSize)

    new SpriteSheetFrameCoordinateOffsets(scaleFactor, translationFactor, f)
  }

  def defaultOffset: SpriteSheetFrameCoordinateOffsets =
    new SpriteSheetFrameCoordinateOffsets(
      scale = Vector2.one,
      translate = Vector2.zero,
      translateCoords = identity
    )

  final class SpriteSheetFrameCoordinateOffsets(val scale: Vector2, val translate: Vector2, translateCoords: Vector2 => Vector2) {
    def offsetToCoords(offset: Vector2): Vector2 =
      translateCoords(offset)
  }
  object SpriteSheetFrameCoordinateOffsets {

    implicit val show: AsString[SpriteSheetFrameCoordinateOffsets] = {
      val sv = implicitly[AsString[Vector2]]
      AsString.create(v => s"SpriteSheetFrameCoordinateOffsets(scale = ${sv.show(v.scale)}, translate = ${sv.show(v.translate)})")
    }

    implicit val eq: EqualTo[SpriteSheetFrameCoordinateOffsets] = {
      val ev = implicitly[EqualTo[Vector2]]

      EqualTo.create { (a, b) =>
        ev.equal(a.scale, b.scale) && ev.equal(a.translate, b.translate)
      }
    }

  }

}
