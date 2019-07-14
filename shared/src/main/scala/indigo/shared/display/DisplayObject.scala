package indigo.shared.display

final class DisplayObject(
    val x: Double,
    val y: Double,
    val z: Double,
    val width: Double,
    val height: Double,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val imageRef: String,
    val alpha: Double,
    val tintR: Double,
    val tintG: Double,
    val tintB: Double,
    val flipHorizontal: Double,
    val flipVertical: Double,
    val frameX: Double,
    val frameY: Double,
    val frameScaleX: Double,
    val frameScaleY: Double
)
object DisplayObject {

  def apply(
      x: Int,
      y: Int,
      z: Int,
      width: Int,
      height: Int,
      rotation: Double,
      scaleX: Double,
      scaleY: Double,
      imageRef: String,
      alpha: Double,
      tintR: Double,
      tintG: Double,
      tintB: Double,
      flipHorizontal: Boolean,
      flipVertical: Boolean,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
  ): DisplayObject =
    new DisplayObject(
      x.toDouble,
      y.toDouble,
      z.toDouble,
      width.toDouble,
      height.toDouble,
      rotation,
      scaleX,
      scaleY,
      imageRef,
      alpha,
      tintR,
      tintG,
      tintB,
      if (flipHorizontal) -1 else 1,
      if (flipVertical) 1 else -1,
      frame.translate.x,
      frame.translate.y,
      frame.scale.x,
      frame.scale.y
    )
}
