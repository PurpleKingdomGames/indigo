package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.FontKey

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class BoundaryLocatorDelegate(boundaryLocator: BoundaryLocator) {

  @JSExport
  def findBounds(sceneGraphNode: SceneGraphNodeDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.findBounds(sceneGraphNode.toInternal)
    )

  @JSExport
  def groupBounds(group: GroupDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.groupBounds(group.toInternal)
    )

  @JSExport
  def graphicBounds(graphic: GraphicDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.graphicBounds(graphic.toInternal)
    )

  @JSExport
  def spriteBounds(sprite: SpriteDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.spriteBounds(sprite.toInternal)
    )

  @JSExport
  def textLineBounds(lineText: String, fontInfo: FontInfoDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.textLineBounds(lineText, fontInfo.toInternal)
    )

  @JSExport
  def textAsLinesWithBounds(text: String, fontKey: String): List[TextLineDelegate] =
    boundaryLocator
      .textAsLinesWithBounds(text, FontKey(fontKey))
      .map(TextLineDelegate.fromInternal)

  @JSExport
  def textBoundsUnaligned(text: String, fontKey: String, position: PointDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.textBoundsUnaligned(text, FontKey(fontKey), position.toInternal)
    )

  @JSExport
  def textBounds(text: TextDelegate): RectangleDelegate =
    RectangleDelegate.fromRectangle(
      boundaryLocator.textBounds(text.toInternal)
    )

}
