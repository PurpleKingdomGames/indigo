package indigo.shared.display

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
    val diffuseRef: String,
    val emissionRef: String,
    val normalRef: String,
    val specularRef: String,
    val frameX: Double,
    val frameY: Double,
    val frameScaleX: Double,
    val frameScaleY: Double,
    val refX: Double,
    val refY: Double,
    val effects: DisplayEffects
) extends DisplayEntity {
  val textureHash: String = diffuseRef+emissionRef+normalRef+specularRef
}
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
      diffuseRef: String,
      emissionRef: String,
      normalRef: String,
      specularRef: String,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets,
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
      diffuseRef,
      emissionRef,
      normalRef,
      specularRef,
      frame.translate.x,
      frame.translate.y,
      frame.scale.x,
      frame.scale.y,
      refX.toDouble,
      refY.toDouble,
      effects
    )
}
