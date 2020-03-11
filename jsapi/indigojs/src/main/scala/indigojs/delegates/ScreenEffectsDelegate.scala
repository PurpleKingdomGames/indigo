package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.ScreenEffects

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ScreenEffects")
final class ScreenEffectsDelegate(_gameColorOverlay: TintDelegate, _uiColorOverlay: TintDelegate) {

  @JSExport
  val gameColorOverlay = _gameColorOverlay
  @JSExport
  val uiColorOverlay = _uiColorOverlay

  @JSExport
  def withGameColorOverlay(overlay: TintDelegate): ScreenEffectsDelegate =
    new ScreenEffectsDelegate(overlay, uiColorOverlay)

  @JSExport
  def withUiColorOverlay(overlay: TintDelegate): ScreenEffectsDelegate =
    new ScreenEffectsDelegate(gameColorOverlay, overlay)

  @JSExport
  def concat(other: ScreenEffectsDelegate): ScreenEffectsDelegate =
    new ScreenEffectsDelegate(gameColorOverlay.concat(other.gameColorOverlay), uiColorOverlay.concat(other.uiColorOverlay))


  def toInternal: ScreenEffects =
    ScreenEffects(gameColorOverlay.toInternal, uiColorOverlay.toInternal)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("ScreenEffectsHelper")
@JSExportAll
object ScreenEffectsDelegate {

  def None: ScreenEffectsDelegate =
    new ScreenEffectsDelegate(TintDelegate.Zero, TintDelegate.Zero)

}
