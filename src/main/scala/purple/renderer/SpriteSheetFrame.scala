package purple.renderer

object SpriteSheetFrame {

  def calculateFrameOffset(imageSize: Vector2, frameSize: Vector2, framePosition: Vector2): SpriteSheetFrameCoordinateOffsets = {
    val scaleFactor = Vector2.divide(frameSize, imageSize)
    val frameOffsetFactor = Vector2.divide(framePosition, frameSize)
    val translationFactor = scaleFactor.scale(frameOffsetFactor)

    SpriteSheetFrameCoordinateOffsets(scaleFactor, translationFactor)
  }

  case class SpriteSheetFrameCoordinateOffsets(scale: Vector2, translate: Vector2)

  def defaultOffset: SpriteSheetFrameCoordinateOffsets = SpriteSheetFrameCoordinateOffsets(
    scale = Vector2(1, 1),
    translate = Vector2(0, 0)
  )

}
