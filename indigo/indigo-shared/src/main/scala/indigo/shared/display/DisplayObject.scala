package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4

import scala.scalajs.js
import scalajs.js.JSConverters._

sealed abstract class DisplayEntity extends js.Object {
  def z: Double
  def applyTransform(matrix: CheapMatrix4): DisplayEntity
}

final class DisplayClone(
    val id: String,
    val transform: js.Array[Double],
    val z: Double,
    val alpha: Float
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayClone =
    new DisplayClone(id, (CheapMatrix4(transform.toArray) * matrix).mat.toJSArray, z, alpha)
}
object DisplayClone {
  def asBatchData(dc: DisplayClone): DisplayCloneBatchData =
    new DisplayCloneBatchData(
      dc.transform,
      dc.alpha
    )
}

final class DisplayCloneBatchData(
    val transform: js.Array[Double],
    val alpha: Float
) extends js.Object {
  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatchData =
    new DisplayCloneBatchData((CheapMatrix4(transform.toArray) * matrix).mat.toJSArray, alpha)
}
object DisplayCloneBatchData {
  val None: DisplayCloneBatchData =
    new DisplayCloneBatchData(CheapMatrix4.identity.mat.toJSArray, 0.0f)
}
final class DisplayCloneBatch(
    val id: String,
    val z: Double,
    val clones: List[DisplayCloneBatchData]
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatch =
    new DisplayCloneBatch(id, z, clones.map(_.applyTransform(matrix)))
}

final class DisplayObject(
    val transform: js.Array[Double],
    val z: Double,
    val width: Float,
    val height: Float,
    val atlasName: String,
    val frameX: Float,
    val frameY: Float,
    val frameScaleX: Float,
    val frameScaleY: Float,
    val albedoAmount: Float,
    val emissiveOffsetX: Float,
    val emissiveOffsetY: Float,
    val emissiveAmount: Float,
    val normalOffsetX: Float,
    val normalOffsetY: Float,
    val normalAmount: Float,
    val specularOffsetX: Float,
    val specularOffsetY: Float,
    val specularAmount: Float,
    val isLit: Float,
    val effects: DisplayEffects
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayObject =
    new DisplayObject(
      (CheapMatrix4(transform.toArray) * matrix).mat.toJSArray,
      z,
      width,
      height,
      atlasName,
      frameX,
      frameY,
      frameScaleX,
      frameScaleY,
      albedoAmount,
      emissiveOffsetX,
      emissiveOffsetY,
      emissiveAmount,
      normalOffsetX,
      normalOffsetY,
      normalAmount,
      specularOffsetX,
      specularOffsetY,
      specularAmount,
      isLit,
      effects
    )

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
    new DisplayObject(
      transform.mat.toJSArray,
      z,
      width.toFloat,
      height.toFloat,
      atlasName,
      frame.translate.x.toFloat,
      frame.translate.y.toFloat,
      frame.scale.x.toFloat,
      frame.scale.y.toFloat,
      albedoAmount,
      emissiveOffset.x.toFloat,
      emissiveOffset.y.toFloat,
      emissiveAmount,
      normalOffset.x.toFloat,
      normalOffset.y.toFloat,
      normalAmount,
      specularOffset.x.toFloat,
      specularOffset.y.toFloat,
      specularAmount,
      isLit,
      effects
    )
}
