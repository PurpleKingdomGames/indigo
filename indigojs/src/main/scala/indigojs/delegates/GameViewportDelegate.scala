package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.config.GameViewport

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameViewport")
final class GameViewportDelegate(_width: Int, _height: Int) {

  @JSExport
  val width = _width
  @JSExport
  val height = _height

  def toInternal: GameViewport =
    new GameViewport(width, height)
}
