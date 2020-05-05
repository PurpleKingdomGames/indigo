package indigo.shared.display

import indigo.shared.datatypes.Vector2

sealed trait DisplayEntity {
  val z: Float
}

final class DisplayClone(val id: String, val x: Float, val y: Float, val z: Float, val rotation: Float, val scaleX: Float, val scaleY: Float, val alpha: Float, val flipHorizontal: Float, val flipVertical: Float) extends DisplayEntity {
  def asBatchData: DisplayCloneBatchData =
    new DisplayCloneBatchData(x, y, rotation, scaleX, scaleY, alpha, flipHorizontal, flipVertical)
}

final class DisplayCloneBatchData(val x: Float, val y: Float, val rotation: Float, val scaleX: Float, val scaleY: Float, val alpha: Float, val flipHorizontal: Float, val flipVertical: Float)
final class DisplayCloneBatch(val id: String, val z: Float, val clones: List[DisplayCloneBatchData]) extends DisplayEntity

final class DisplayObject(
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val scaleX: Float,
    val scaleY: Float,
    val atlasName: String,
    val frameX: Float,
    val frameY: Float,
    val frameScaleX: Float,
    val frameScaleY: Float,
    val albedoAmount: Float,
    val emissiveOffset: Vector2,
    val emissiveAmount: Float,
    val normalOffset: Vector2,
    val normalAmount: Float,
    val specularOffset: Vector2,
    val specularAmount: Float,
    val isLit: Float,
    val refX: Float,
    val refY: Float,
    val effects: DisplayEffects
) extends DisplayEntity
object DisplayObject {

  def apply(
      x: Int,
      y: Int,
      z: Int,
      width: Int,
      height: Int,
      rotation: Float,
      scaleX: Float,
      scaleY: Float,
      atlasName: String,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      albedoAmount: Float,
      emissiveOffset: Vector2,
      emissiveAmount: Float,
      normalOffset: Vector2,
      normalAmount: Float,
      specularOffset: Vector2,
      specularAmount: Float,
      isLit: Float,
      refX: Int,
      refY: Int,
      effects: DisplayEffects
  ): DisplayObject =
    new DisplayObject(
      x.toFloat,
      y.toFloat,
      z.toFloat,
      width.toFloat,
      height.toFloat,
      rotation,
      scaleX,
      scaleY,
      atlasName,
      frame.translate.x.toFloat,
      frame.translate.y.toFloat,
      frame.scale.x.toFloat,
      frame.scale.y.toFloat,
      albedoAmount,
      emissiveOffset,
      emissiveAmount,
      normalOffset,
      normalAmount,
      specularOffset,
      specularAmount,
      isLit,
      refX.toFloat,
      refY.toFloat,
      effects
    )
}
