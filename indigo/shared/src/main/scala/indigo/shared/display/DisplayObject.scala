package indigo.shared.display

import indigo.shared.datatypes.Vector2

sealed trait DisplayEntity {
  val z: Double
}

final class DisplayClone(val id: String, val x: Double, val y: Double, val z: Double, val rotation: Double, val scaleX: Double, val scaleY: Double) extends DisplayEntity {
  def asBatchData: DisplayCloneBatchData =
    new DisplayCloneBatchData(x, y, rotation, scaleX, scaleY)
}

final class DisplayCloneBatchData(val x: Double, val y: Double, val rotation: Double, val scaleX: Double, val scaleY: Double)
final class DisplayCloneBatch(val id: String, val z: Double, val clones: List[DisplayCloneBatchData]) extends DisplayEntity

final class DisplayObject(
    val x: Double,
    val y: Double,
    val z: Double,
    val width: Double,
    val height: Double,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val atlasName: String,
    val frameX: Double,
    val frameY: Double,
    val frameScaleX: Double,
    val frameScaleY: Double,
    val albedoAmount: Double,
    val emissiveOffset: Vector2,
    val emissiveAmount: Double,
    val normalOffset: Vector2,
    val normalAmount: Double,
    val specularOffset: Vector2,
    val specularAmount: Double,
    val isLit: Double,
    val refX: Double,
    val refY: Double,
    val effects: DisplayEffects
) extends DisplayEntity
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
      atlasName: String,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      albedoAmount: Double,
      emissiveOffset: Vector2,
      emissiveAmount: Double,
      normalOffset: Vector2,
      normalAmount: Double,
      specularOffset: Vector2,
      specularAmount: Double,
      isLit: Double,
      refX: Int,
      refY: Int,
      effects: DisplayEffects
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
      atlasName,
      frame.translate.x,
      frame.translate.y,
      frame.scale.x,
      frame.scale.y,
      albedoAmount,
      emissiveOffset,
      emissiveAmount,
      normalOffset,
      normalAmount,
      specularOffset,
      specularAmount,
      isLit,
      refX.toDouble,
      refY.toDouble,
      effects
    )
}
