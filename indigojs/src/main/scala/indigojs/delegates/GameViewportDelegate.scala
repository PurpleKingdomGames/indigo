package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.config.GameViewport

@JSExportTopLevel("GameViewport")
final class GameViewportDelegate(width: Int, height: Int) {
  def toInternal: GameViewport =
    new GameViewport(width, height)
}
