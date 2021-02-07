package indigo.shared.display

import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.shader.ShaderId

sealed trait DisplayEntity {
  def z: Double
  def applyTransform(matrix: CheapMatrix4): DisplayEntity
}

final case class DisplayClone(
    val id: String,
    val transform: CheapMatrix4,
    val z: Double//,
    // val alpha: Float
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayClone =
    this.copy(transform = transform * matrix)
}
object DisplayClone {
  def asBatchData(dc: DisplayClone): CheapMatrix4 =
    // new DisplayCloneBatchData(
      dc.transform//,
    //   dc.alpha
    // )
}

// final case class DisplayCloneBatchData(
//     val transform: CheapMatrix4,
//     val alpha: Float
// ) {
//   def applyTransform(matrix: CheapMatrix4): DisplayCloneBatchData =
//     this.copy(transform = transform * matrix)
// }
// object DisplayCloneBatchData {
//   val None: DisplayCloneBatchData =
//     DisplayCloneBatchData(CheapMatrix4.identity, 0.0f)
// }
final case class DisplayCloneBatch(
    val id: String,
    val z: Double,
    val clones: List[CheapMatrix4]
) extends DisplayEntity {

  def applyTransform(matrix: CheapMatrix4): DisplayCloneBatch =
    this.copy(clones = clones.map(_ * matrix))
    // this.copy(clones = clones.map(_.applyTransform(matrix)))
}

final case class DisplayObject(
    transform: CheapMatrix4,
    z: Double,
    width: Float,
    height: Float,
    atlasName: Option[String],
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
    isLit: Float,
    shaderId: ShaderId,
    shaderUniformHash: String,
    shaderUBO: Array[Float]
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
      atlasName: Option[String],
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
      channelOffset1: Vector2,
      channelOffset2: Vector2,
      channelOffset3: Vector2,
      isLit: Float,
      shaderId: ShaderId,
      shaderUniformHash: String,
      shaderUBO: Array[Float]
  ): DisplayObject =
    DisplayObject(
      transform,
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
      isLit,
      shaderId,
      shaderUniformHash,
      shaderUBO
    )
}

// final case class DisplayObjectShape(
//     val transform: CheapMatrix4,
//     val z: Double,
//     val width: Float,
//     val height: Float
// ) extends DisplayEntity {

//   def applyTransform(matrix: CheapMatrix4): DisplayObjectShape =
//     this.copy(transform * matrix)

// }
