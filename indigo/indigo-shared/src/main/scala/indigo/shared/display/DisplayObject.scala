package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Matrix4

sealed trait DisplayEntity {
  def z: Double
}

final class DisplayClone(
    val id: String,
    val transform: Matrix4,
    val alpha: Float
) extends DisplayEntity {
  def z: Double = transform.z
}
object DisplayClone {
  def asBatchData(dc: DisplayClone): DisplayCloneBatchData =
    new DisplayCloneBatchData(
      dc.transform,
      dc.alpha
    )
}

final class DisplayCloneBatchData(
    val transform: Matrix4,
    val alpha: Float
)
final class DisplayCloneBatch(
    val id: String,
    val z: Double,
    val clones: List[DisplayCloneBatchData]
) extends DisplayEntity

final class DisplayObject(
    val transform: Matrix4,
    val width: Float,
    val height: Float,
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
    val effects: DisplayEffects
) extends DisplayEntity {
  def z: Double = transform.z
}
object DisplayObject {

  def apply(
      transform: Matrix4,
      width: Int,
      height: Int,
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
      effects: DisplayEffects
  ): DisplayObject =
    new DisplayObject(
      transform,
      width.toFloat,
      height.toFloat,
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
      effects
    )
}
