package indigo.platform.renderer

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.display.DisplayEffects

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      rotation = 0,
      scaleX = 1,
      scaleY = 1,
      diffuseRef = "",
      frame = SpriteSheetFrame.defaultOffset,
      emissionOffset = Vector2.minusOne,
      normalOffset = Vector2.minusOne,
      specularOffset = Vector2.minusOne,
      isLit = 0.0,
      refX = 0,
      refY = 0,
      effects = DisplayEffects.default
    )

}
