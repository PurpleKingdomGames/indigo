package indigo.platform.renderer.shared

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.display.DisplayEffects
import indigo.shared.datatypes.Matrix4

import scalajs.js.JSConverters._
import scala.collection.mutable
import indigo.shared.display.DisplayEntity

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      // x = 0,
      // y = 0,
      // z = 1,
      transform = makeMatrix(w.toDouble, h.toDouble),
      width = w,
      height = h,
      // rotation = 0,
      // scaleX = 1,
      // scaleY = 1,
      atlasName = "",
      frame = SpriteSheetFrame.defaultOffset,
      albedoAmount = 1.0f,
      emissiveOffset = Vector2.zero,
      emissiveAmount = 0.0f,
      normalOffset = Vector2.zero,
      normalAmount = 0.0f,
      specularOffset = Vector2.zero,
      specularAmount = 0.0f,
      isLit = 0.0f,
      // refX = 0,
      // refY = 0,
      effects = DisplayEffects.default
      // flipHorizontal = 1.0f,
      // flipVertical = -1.0f
    )

  private def makeMatrix(w: Double, h: Double): Matrix4 =
    Matrix4
      .translation(Vector3(0.5d, 0.5d, 0.0d)) // reposition on screen
      .scale(Vector3(w, h, 1.0))

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] =
    mat4d.toList.toJSArray

  val sortByDepth: mutable.ListBuffer[DisplayEntity] => mutable.ListBuffer[DisplayEntity] =
    _.sortWith((d1, d2) => d1.z > d2.z)

}
