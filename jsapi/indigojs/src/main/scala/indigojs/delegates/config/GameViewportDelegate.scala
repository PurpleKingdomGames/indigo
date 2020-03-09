package indigojs.delegates.config

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

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameViewportHelper")
object GameViewportDelegate {
  @JSExport
  val atWUXGA: GameViewportDelegate = new GameViewportDelegate(1920, 1200)

  @JSExport
  val atWUXGABy2: GameViewportDelegate = new GameViewportDelegate(960, 600)

  @JSExport
  val at1080p: GameViewportDelegate = new GameViewportDelegate(1920, 1080)

  @JSExport
  val at1080pBy2: GameViewportDelegate = new GameViewportDelegate(960, 540)

  @JSExport
  val at720p: GameViewportDelegate = new GameViewportDelegate(1280, 720)

  @JSExport
  val at720pBy2: GameViewportDelegate = new GameViewportDelegate(640, 360)
}
