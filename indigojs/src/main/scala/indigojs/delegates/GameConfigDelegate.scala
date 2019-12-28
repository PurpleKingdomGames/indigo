package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.config.GameConfig

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfig")
final class GameConfigDelegate(_viewport: GameViewportDelegate, _frameRate: Int, _clearColor: ClearColorDelegate, _magnification: Int) {

  @JSExport
  val viewport = _viewport
  @JSExport
  val frameRate = _frameRate
  @JSExport
  val clearColor = _clearColor
  @JSExport
  val magnification = _magnification

  @JSExport
  def withViewport(newViewport: GameViewportDelegate): GameConfigDelegate =
    new GameConfigDelegate(newViewport, frameRate, clearColor, magnification)

  @JSExport
  def withFrameRate(newFrameRate: Int): GameConfigDelegate =
    new GameConfigDelegate(viewport, newFrameRate, clearColor, magnification)

  @JSExport
  def withClearColor(newClearColor: ClearColorDelegate): GameConfigDelegate =
    new GameConfigDelegate(viewport, frameRate, newClearColor, magnification)

  @JSExport
  def withMagnification(newMagnification: Int): GameConfigDelegate =
    new GameConfigDelegate(viewport, frameRate, clearColor, newMagnification)

  def toInternal: GameConfig =
    GameConfig(viewport.toInternal, frameRate, clearColor.toInternal, magnification)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfigHelper")
object GameConfigDelegate {

  @JSExport
  val default: GameConfigDelegate =
    new GameConfigDelegate(
      new GameViewportDelegate(550, 400),
      60,
      new ClearColorDelegate(0, 0, 0, 1),
      1
    )
}
