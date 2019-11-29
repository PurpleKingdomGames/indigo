package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.config.GameConfig
import indigo.shared.config.GameViewport
import indigo.shared.ClearColor

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfig")
final class GameConfigDelegate(viewport: GameViewportDelegate, frameRate: Int, clearColor: ClearColorDelegate, magnification: Int) {

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
@JSExportTopLevel("GameConfigOps")
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

@JSExportTopLevel("GameViewport")
final class GameViewportDelegate(width: Int, height: Int) {
  def toInternal: GameViewport =
    new GameViewport(width, height)
}

@JSExportTopLevel("ClearColor")
final class ClearColorDelegate(r: Double, g: Double, b: Double, a: Double) {
  def toInternal: ClearColor =
    new ClearColor(r, g, b, a)
}
