package indigo.shared.scenegraph

import indigo.shared.datatypes.RGBA

final case class ScreenEffects(gameColorOverlay: RGBA, uiColorOverlay: RGBA) {

  def |+|(other: ScreenEffects): ScreenEffects =
    ScreenEffects(gameColorOverlay + other.gameColorOverlay, uiColorOverlay + other.uiColorOverlay)

  def withGameColorOverlay(overlay: RGBA): ScreenEffects =
    this.copy(gameColorOverlay = overlay)

  def withUiColorOverlay(overlay: RGBA): ScreenEffects =
    this.copy(uiColorOverlay = overlay)

}

object ScreenEffects {

  def None: ScreenEffects =
    ScreenEffects(RGBA.Zero, RGBA.Zero)

}
