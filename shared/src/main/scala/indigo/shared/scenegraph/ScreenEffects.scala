package indigo.shared.scenegraph

import indigo.shared.datatypes.Tint

final class ScreenEffects(val gameColorOverlay: Tint, val uiColorOverlay: Tint) {

  def |+|(other: ScreenEffects): ScreenEffects =
    ScreenEffects(gameColorOverlay + other.gameColorOverlay, uiColorOverlay + other.uiColorOverlay)

  def withGameColorOverlay(overlay: Tint): ScreenEffects =
    ScreenEffects(overlay, uiColorOverlay)

  def withUiColorOverlay(overlay: Tint): ScreenEffects =
    ScreenEffects(gameColorOverlay, overlay)

}

object ScreenEffects {

  def apply(gameColorOverlay: Tint, uiColorOverlay: Tint): ScreenEffects =
    new ScreenEffects(gameColorOverlay, uiColorOverlay)

  def None: ScreenEffects =
    ScreenEffects(Tint.Zero, Tint.Zero)

}
