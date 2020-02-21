package indigo.shared.display

import indigo.shared.datatypes.Effects

final class DisplayEffects(
    val tint: Array[Double],
    val colorOverlay: Array[Double],
    val gradiantOverlayFrom: Array[Double],
    val gradiantOverlayTo: Array[Double],
    val gradiantOverlayFromColor: Array[Double],
    val gradiantOverlayToColor: Array[Double],
    val outerBorderColor: Array[Double],
    val outerBorderAmount: Double,
    val innerBorderColor: Array[Double],
    val innerBorderAmount: Double,
    val outerGlowColor: Array[Double],
    val outerGlowAmount: Double,
    val innerGlowColor: Array[Double],
    val innerGlowAmount: Double,
    val blur: Double,
    val alpha: Double,
    val flipHorizontal: Double,
    val flipVertical: Double
)

object DisplayEffects {

  def fromEffects(effects: Effects): DisplayEffects =
    new DisplayEffects(
      effects.tint.toArray,
      effects.colorOverlay.toArray,
      Array(effects.gradiantOverlay.fromPoint.x.toDouble, effects.gradiantOverlay.fromPoint.y.toDouble),
      Array(effects.gradiantOverlay.toPoint.x.toDouble, effects.gradiantOverlay.toPoint.y.toDouble),
      effects.gradiantOverlay.fromColor.toArray,
      effects.gradiantOverlay.toColor.toArray,
      effects.outerBorder.color.toArray,
      effects.outerBorder.amount,
      effects.innerBorder.color.toArray,
      effects.innerBorder.amount,
      effects.outerGlow.color.toArray,
      effects.outerGlow.amount,
      effects.innerGlow.color.toArray,
      effects.innerGlow.amount,
      effects.blur,
      effects.alpha,
      if (effects.flip.horizontal) -1 else 1,
      if (effects.flip.vertical) 1 else -1
    )

  val default: DisplayEffects =
    DisplayEffects.fromEffects(Effects.default)

}
