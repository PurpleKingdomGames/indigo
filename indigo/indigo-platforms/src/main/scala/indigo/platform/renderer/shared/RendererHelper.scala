package indigo.platform.renderer.shared

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4

import scala.collection.mutable
import indigo.shared.display.DisplayEntity
import indigo.shared.shader.ShaderId

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      transform = makeMatrix(w.toDouble, h.toDouble),
      z = 1,
      width = w,
      height = h,
      atlasName = None,
      frame = SpriteSheetFrame.defaultOffset,
      channelOffset1 = Vector2.zero,
      channelOffset2 = Vector2.zero,
      channelOffset3 = Vector2.zero,
      isLit = 0.0f,
      shaderId = ShaderId(""),
      shaderUniformData = None
    )

  private def makeMatrix(w: Double, h: Double): CheapMatrix4 =
    CheapMatrix4.identity
      .translate(0.5d, 0.5d, 0.0d) // reposition on screen
      .scale(w, h, 1.0)

  val sortByDepth: mutable.ListBuffer[DisplayEntity] => mutable.ListBuffer[DisplayEntity] =
    _.sortWith((d1, d2) => d1.z > d2.z)

}
