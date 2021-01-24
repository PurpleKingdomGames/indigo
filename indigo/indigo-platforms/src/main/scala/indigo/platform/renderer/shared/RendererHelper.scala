package indigo.platform.renderer.shared

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.display.DisplayEffects
import indigo.shared.datatypes.mutable.CheapMatrix4

import scalajs.js.JSConverters._
import scala.collection.mutable
import indigo.shared.display.DisplayEntity

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      transform = makeMatrix(w.toDouble, h.toDouble),
      z = 1,
      width = w,
      height = h,
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
      effects = DisplayEffects.default,
      shaderId = None
    )

  private def makeMatrix(w: Double, h: Double): CheapMatrix4 =
    CheapMatrix4.identity
      .translate(0.5d, 0.5d, 0.0d) // reposition on screen
      .scale(w, h, 1.0)

  def mat4ToJsArray(mat4d: CheapMatrix4): scalajs.js.Array[Double] =
    mat4d.mat.toJSArray

  val sortByDepth: mutable.ListBuffer[DisplayEntity] => mutable.ListBuffer[DisplayEntity] =
    _.sortWith((d1, d2) => d1.z > d2.z)

}
