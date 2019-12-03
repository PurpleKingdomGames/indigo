package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.ScreenEffects

@JSExportTopLevel("ScreenEffects")
final class ScreenEffectsDelegate(val gameColorOverlay: TintDelegate, val uiColorOverlay: TintDelegate) {

  def withGameColorOverlay(overlay: TintDelegate): ScreenEffectsDelegate =
    new ScreenEffectsDelegate(overlay, uiColorOverlay)

  def withUiColorOverlay(overlay: TintDelegate): ScreenEffectsDelegate =
    new ScreenEffectsDelegate(gameColorOverlay, overlay)

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
