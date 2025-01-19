package indigo.shared.display

import indigo.platform.assets.AtlasId
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.TextStyle
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.scenegraph.CloneBatchData
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.CloneTileData
import indigo.shared.shader.ShaderId

sealed trait DisplayEntity:
  def z: Double

final case class DisplayGroup(
    transform: CheapMatrix4,
    z: Double,
    entities: scalajs.js.Array[DisplayEntity]
) extends DisplayEntity derives CanEqual
object DisplayGroup:
  val empty: DisplayGroup =
    DisplayGroup(CheapMatrix4.identity, 0.0d, scalajs.js.Array())

final case class DisplayTextLetters(letters: scalajs.js.Array[DisplayEntity]) extends DisplayEntity derives CanEqual:
  val z: Double = 0.0d // Not used
object DisplayTextLetters:
  val empty: DisplayTextLetters =
    DisplayTextLetters(scalajs.js.Array())

final case class DisplayCloneBatch(
    id: CloneId,
    z: Double,
    cloneData: scalajs.js.Array[CloneBatchData]
) extends DisplayEntity derives CanEqual

final case class DisplayCloneTiles(
    id: CloneId,
    z: Double,
    cloneData: scalajs.js.Array[CloneTileData]
) extends DisplayEntity derives CanEqual

final case class DisplayMutants(
    id: CloneId,
    z: Double,
    cloneData: scalajs.js.Array[scalajs.js.Array[DisplayObjectUniformData]]
) extends DisplayEntity derives CanEqual

final case class DisplayObject(
    x: Float,
    y: Float,
    scaleX: Float,
    scaleY: Float,
    refX: Float,
    refY: Float,
    flipX: Float,
    flipY: Float,
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
    textureX: Float,
    textureY: Float,
    textureWidth: Float,
    textureHeight: Float,
    atlasWidth: Float,
    atlasHeight: Float,
    shaderId: ShaderId,
    shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
) extends DisplayEntity derives CanEqual
object DisplayObject:

  given CanEqual[Option[DisplayObject], Option[DisplayObject]] = CanEqual.derived

  def apply(
      x: Float,
      y: Float,
      scaleX: Float,
      scaleY: Float,
      refX: Float,
      refY: Float,
      flipX: Float,
      flipY: Float,
      rotation: Radians,
      z: Double,
      width: Int,
      height: Int,
      atlasName: Option[AtlasId],
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      channelOffset1: Vector2,
      channelOffset2: Vector2,
      channelOffset3: Vector2,
      texturePosition: Vector2,
      textureSize: Vector2,
      atlasSize: Vector2,
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): DisplayObject =
    DisplayObject(
      x,
      y,
      scaleX,
      scaleY,
      refX,
      refY,
      flipX,
      flipY,
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
      texturePosition.x.toFloat,
      texturePosition.y.toFloat,
      textureSize.x.toFloat,
      textureSize.y.toFloat,
      atlasSize.x.toFloat,
      atlasSize.y.toFloat,
      shaderId,
      shaderUniformData
    )

final case class DisplayObjectUniformData(uniformHash: String, blockName: String, data: scalajs.js.Array[Float])
    derives CanEqual

final case class DisplayText(
    text: String,
    style: TextStyle,
    x: Float,
    y: Float,
    scaleX: Float,
    scaleY: Float,
    refX: Float,
    refY: Float,
    flipX: Float,
    flipY: Float,
    rotation: Radians,
    z: Double,
    width: Int,
    height: Int
) extends DisplayEntity derives CanEqual
