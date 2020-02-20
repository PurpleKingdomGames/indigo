package indigo.shared.scenegraph

import indigo.shared.datatypes.RGBA

final class ScreenEffects(val gameColorOverlay: RGBA, val uiColorOverlay: RGBA) {

  def |+|(other: ScreenEffects): ScreenEffects =
    ScreenEffects(gameColorOverlay + other.gameColorOverlay, uiColorOverlay + other.uiColorOverlay)

  def withGameColorOverlay(overlay: RGBA): ScreenEffects =
    ScreenEffects(overlay, uiColorOverlay)

  def withUiColorOverlay(overlay: RGBA): ScreenEffects =
    ScreenEffects(gameColorOverlay, overlay)

}

object ScreenEffects {

  def apply(gameColorOverlay: RGBA, uiColorOverlay: RGBA): ScreenEffects =
    new ScreenEffects(gameColorOverlay, uiColorOverlay)

  def None: ScreenEffects =
    ScreenEffects(RGBA.Zero, RGBA.Zero)

}
