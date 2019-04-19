package indigo.gameengine.display

import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.renderer.SpriteSheetFrame

final class Displayable(val game: DisplayLayer, val lighting: DisplayLayer, val ui: DisplayLayer, val ambientLight: AmbientLight)
object Displayable {
  def apply(game: DisplayLayer, lighting: DisplayLayer, ui: DisplayLayer, ambientLight: AmbientLight): Displayable =
    new Displayable(game, lighting, ui, ambientLight)
}

final class DisplayLayer(val displayObjects: List[DisplayObject]) extends AnyVal
object DisplayLayer {
  def apply(displayObjects: List[DisplayObject]): DisplayLayer =
    new DisplayLayer(displayObjects)
}

final class DisplayObject(
    val x: Int,
    val y: Int,
    val z: Int,
    val width: Int,
    val height: Int,
    val imageRef: String,
    val alpha: Double,
    val tintR: Double,
    val tintG: Double,
    val tintB: Double,
    val flipHorizontal: Boolean,
    val flipVertical: Boolean,
    val frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
)
object DisplayObject {
  def apply(
      x: Int,
      y: Int,
      z: Int,
      width: Int,
      height: Int,
      imageRef: String,
      alpha: Double,
      tintR: Double,
      tintG: Double,
      tintB: Double,
      flipHorizontal: Boolean,
      flipVertical: Boolean,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
  ): DisplayObject =
    new DisplayObject(
      x,
      y,
      z,
      width,
      height,
      imageRef,
      alpha,
      tintR,
      tintG,
      tintB,
      flipHorizontal,
      flipVertical,
      frame
    )
}
