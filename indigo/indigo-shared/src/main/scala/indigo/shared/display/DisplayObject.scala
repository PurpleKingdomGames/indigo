package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4

sealed trait DisplayEntity {
  def z: Double
  def applyTransform(matrix: CheapMatrix4): DisplayEntity
}

final case class DisplayClone(
    val id: String,
    val transform: CheapMatrix4,
    val z: Double,
    val alpha: Float
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayClone =
    this.copy(transform = transform * matrix)
}
object DisplayClone {
  def asBatchData(dc: DisplayClone): DisplayCloneBatchData =
    new DisplayCloneBatchData(
      dc.transform,
      dc.alpha
    )
}

final case class DisplayCloneBatchData(
    val transform: CheapMatrix4,
    val alpha: Float
) {
  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatchData =
    this.copy(transform = transform * matrix)
}
object DisplayCloneBatchData {
  val None: DisplayCloneBatchData =
    DisplayCloneBatchData(CheapMatrix4.identity, 0.0f)
}
final case class DisplayCloneBatch(
    val id: String,
    val z: Double,
    val clones: List[DisplayCloneBatchData]
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatch =
    this.copy(clones = clones.map(_.applyTransform(matrix)))
}

final case class DisplayObject(
    val transform: CheapMatrix4,
    val z: Double,
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

  def applyTransform(matrix: CheapMatrix4): DisplayObject =
    this.copy(transform * matrix)

}
object DisplayObject {

  def apply(
      transform: CheapMatrix4,
      z: Double,
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
    DisplayObject(
      transform,
      z,
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

final case class DisplayObjectShape(
    val transform: CheapMatrix4,
    val z: Double,
    val width: Float,
    val height: Float
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayObjectShape =
    this.copy(transform * matrix)

}
