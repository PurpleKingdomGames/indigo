package indigojs.delegates.config

import scala.scalajs.js.annotation._
import indigojs.delegates.ClearColorDelegate
import indigo.shared.config.GameConfig

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GameConfig")
final class GameConfigDelegate(_viewport: GameViewportDelegate, _frameRate: Int, _clearColor: ClearColorDelegate, _magnification: Int, _advanced: AdvancedGameConfigDelegate) {

  @JSExport
  val viewport = _viewport
  @JSExport
  val frameRate = _frameRate
  @JSExport
  val clearColor = _clearColor
  @JSExport
  val magnification = _magnification
  @JSExport
  val advanced = _advanced

  @JSExport
  val screenDimensions = RectangleDelegate.fromRectangle(toInternal.screenDimensions)

  @JSExport
  def withViewport(newViewport: GameViewportDelegate): GameConfigDelegate =
    new GameConfigDelegate(newViewport, frameRate, clearColor, magnification, advanced)

  @JSExport
  def withFrameRate(newFrameRate: Int): GameConfigDelegate =
    new GameConfigDelegate(viewport, newFrameRate, clearColor, magnification, advanced)

  @JSExport
  def withClearColor(newClearColor: ClearColorDelegate): GameConfigDelegate =
    new GameConfigDelegate(viewport, frameRate, newClearColor, magnification, advanced)

  @JSExport
  def withMagnification(newMagnification: Int): GameConfigDelegate =
    new GameConfigDelegate(viewport, frameRate, clearColor, newMagnification, advanced)

  @JSExport
  def withAdvancedConfig(newAdvancedConfig: AdvancedGameConfigDelegate): GameConfigDelegate =
    new GameConfigDelegate(viewport, frameRate, clearColor, magnification, newAdvancedConfig)

  def toInternal: GameConfig =
    new GameConfig(viewport.toInternal, frameRate, clearColor.toInternal, magnification, advanced.toInternal)

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
      1,
      AdvancedGameConfigDelegate.default
    )
}
