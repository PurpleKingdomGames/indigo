package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.TextStyle
import indigo.shared.shader.ShaderId
import indigo.platform.assets.AtlasId
import indigo.shared.datatypes.Radians
import indigo.shared.scenegraph.CloneId
import scala.collection.mutable.ListBuffer

sealed trait DisplayEntity {
  def z: Double
  def applyTransform(matrix: CheapMatrix4): DisplayEntity
}

final case class DisplayGroup(
    transform: CheapMatrix4,
    z: Double,
    entities: ListBuffer[DisplayEntity]
) extends DisplayEntity
    derives CanEqual:
  def applyTransform(matrix: CheapMatrix4): DisplayGroup =
    this.copy(transform = transform * matrix)
object DisplayGroup:
  val empty: DisplayGroup =
    DisplayGroup(CheapMatrix4.identity, 0.0d, ListBuffer())

final case class DisplayCloneBatch(
    val id: CloneId,
    val z: Double,
    val clones: List[CheapMatrix4]
) extends DisplayEntity
    derives CanEqual {

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
    textureWidth: Float,
    textureHeight: Float,
    atlasWidth: Float,
    atlasHeight: Float,
    shaderId: ShaderId,
    shaderUniformData: List[DisplayObjectUniformData]
) extends DisplayEntity
    derives CanEqual {

  def applyTransform(matrix: CheapMatrix4): DisplayObject =
    this.copy(transform = transform * matrix)

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
      textureSize: Vector2,
      atlasSize: Vector2,
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
      textureSize.x.toFloat,
      textureSize.y.toFloat,
      atlasSize.x.toFloat,
      atlasSize.y.toFloat,
      shaderId,
      shaderUniformData
    )
}

final case class DisplayObjectUniformData(uniformHash: String, blockName: String, data: Array[Float]) derives CanEqual

final case class DisplayText(
    text: String,
    style: TextStyle,
    transform: CheapMatrix4,
    rotation: Radians,
    z: Double,
    width: Int,
    height: Int
) extends DisplayEntity
    derives CanEqual {

  def applyTransform(matrix: CheapMatrix4): DisplayText =
    this.copy(transform = transform * matrix)

}
