package purple.renderer

object Frame {

  def calculateFrameOffset(imageSize: Vector2, frameSize: Vector2, framePosition: Vector2): FrameCoordinateOffsets = {
    val scaleFactor = Vector2.divide(frameSize, imageSize)
    val frameOffsetFactor = Vector2.divide(framePosition, frameSize)
    val translationFactor = scaleFactor.scale(frameOffsetFactor)

    FrameCoordinateOffsets(scaleFactor, translationFactor)
  }

  case class FrameCoordinateOffsets(scale: Vector2, translate: Vector2)

}
