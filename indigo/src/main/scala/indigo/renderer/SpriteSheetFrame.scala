package indigo.renderer

import indigo.gameengine.display.Vector2

object SpriteSheetFrame {

  def calculateFrameOffset(imageSize: Vector2, frameSize: Vector2, framePosition: Vector2, textureOffset: Vector2): SpriteSheetFrameCoordinateOffsets = {
    val scaleFactor       = frameSize / imageSize
    val frameOffsetFactor = (framePosition + textureOffset) / frameSize
    val translationFactor = scaleFactor * frameOffsetFactor

    SpriteSheetFrameCoordinateOffsets(scaleFactor, translationFactor)
  }

  def defaultOffset: SpriteSheetFrameCoordinateOffsets = SpriteSheetFrameCoordinateOffsets(
    scale = Vector2.one,
    translate = Vector2.zero
  )

  final case class SpriteSheetFrameCoordinateOffsets(scale: Vector2, translate: Vector2)

}
