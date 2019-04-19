package indigo.renderer

import indigo.gameengine.display.Vector2
import indigo.shared.AsString
import indigo.shared.EqualTo

object SpriteSheetFrame {

  def calculateFrameOffset(imageSize: Vector2, frameSize: Vector2, framePosition: Vector2, textureOffset: Vector2): SpriteSheetFrameCoordinateOffsets = {
    val scaleFactor       = frameSize / imageSize
    val frameOffsetFactor = (framePosition + textureOffset) / frameSize
    val translationFactor = scaleFactor * frameOffsetFactor

    new SpriteSheetFrameCoordinateOffsets(scaleFactor, translationFactor)
  }

  def defaultOffset: SpriteSheetFrameCoordinateOffsets =
    new SpriteSheetFrameCoordinateOffsets(
      scale = Vector2.one,
      translate = Vector2.zero
    )

  final class SpriteSheetFrameCoordinateOffsets(val scale: Vector2, val translate: Vector2)
  object SpriteSheetFrameCoordinateOffsets {

    implicit val show: AsString[SpriteSheetFrameCoordinateOffsets] = {
      val sv = implicitly[AsString[Vector2]]
      AsString.create( v =>
        s"SpriteSheetFrameCoordinateOffsets(scale = ${sv.show(v.scale)}, translate = ${sv.show(v.translate)})"
      )
    }

    implicit val eq: EqualTo[SpriteSheetFrameCoordinateOffsets] = {
      val ev = implicitly[EqualTo[Vector2]]

      EqualTo.create { (a, b) =>
         ev.equal(a.scale, b.scale) && ev.equal(a.translate, b.translate)
      }
    }

  }

}
