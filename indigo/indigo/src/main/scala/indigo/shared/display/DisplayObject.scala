package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.shader.ShaderId
import indigo.platform.assets.AtlasId
import indigo.shared.datatypes.Radians
import indigo.shared.scenegraph.CloneId

sealed trait DisplayEntity {
  def z: Double
  def applyTransform(matrix: CheapMatrix4): DisplayEntity
}

final case class DisplayClone(
    val id: CloneId,
    val transform: CheapMatrix4,
    val z: Double
) extends DisplayEntity derives CanEqual {

  def applyTransform(matrix: CheapMatrix4): DisplayClone =
    this.copy(transform = transform * matrix)
}
object DisplayClone {
  def asBatchData(dc: DisplayClone): CheapMatrix4 =
    dc.transform
}

final case class DisplayCloneBatch(
    val id: CloneId,
    val z: Double,
    val clones: List[CheapMatrix4]
) extends DisplayEntity derives CanEqual {

  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatch =
    this.copy(clones = clones.map(_ * matrix))
}

final case class DisplayObject(
    transform: CheapMatrix4,
    rotation: Radians,
    z: Double,
    width: Float,
    height: Float,
    atlasName: Option[AtlasId],
    frameScaleX: Float,
    frameScaleY: Float,
    channelOffset0X: Float,
    channelOffset0Y: Float,
    channelOffset1X: Float,
    channelOffset1Y: Float,
    channelOffset2X: Float,
    channelOffset2Y: Float,
    channelOffset3X: Float,
    channelOffset3Y: Float,
    shaderId: ShaderId,
    shaderUniformData: List[DisplayObjectUniformData]
) extends DisplayEntity derives CanEqual {

  def applyTransform(matrix: CheapMatrix4): DisplayObject =
    this.copy(transform * matrix)

}
object DisplayObject {

  given CanEqual[Option[DisplayObject], Option[DisplayObject]] = CanEqual.derived

  def apply(
      transform: CheapMatrix4,
      rotation: Radians,
      z: Double,
      width: Int,
      height: Int,
      atlasName: Option[AtlasId],
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      channelOffset1: Vector2,
      channelOffset2: Vector2,
      channelOffset3: Vector2,
      shaderId: ShaderId,
      shaderUniformData: List[DisplayObjectUniformData]
  ): DisplayObject =
    DisplayObject(
      transform,
      rotation,
      z,
      width.toFloat,
      height.toFloat,
      atlasName,
      frame.scale.x.toFloat,
      frame.scale.y.toFloat,
      frame.translate.x.toFloat,
      frame.translate.y.toFloat,
      channelOffset1.x.toFloat,
      channelOffset1.y.toFloat,
      channelOffset2.x.toFloat,
      channelOffset2.y.toFloat,
      channelOffset3.x.toFloat,
      channelOffset3.y.toFloat,
      shaderId,
      shaderUniformData
    )
}

final case class DisplayObjectUniformData(uniformHash: String, blockName: String, data: Array[Float]) derives CanEqual
