package indigo.shared.display

import indigo.shared.datatypes.Effects
import indigo.shared.datatypes.Overlay
import indigo.shared.datatypes.Thickness

final class DisplayEffects(
    val tint: Array[Double],
    val gradiantOverlayPositions: Array[Double],
    val gradiantOverlayFromColor: Array[Double],
    val gradiantOverlayToColor: Array[Double],
    val borderColor: Array[Double],
    val innerBorderAmount: Double,
    val outerBorderAmount: Double,
    val glowColor: Array[Double],
    val innerGlowAmount: Double,
    val outerGlowAmount: Double,
    val alpha: Double,
    val flipHorizontal: Double,
    val flipVertical: Double
)

object DisplayEffects {

  private val overlayToPositionsArray: Overlay => Array[Double] = {
    case Overlay.Color(_) =>
      Array(0.0, 0.0, 1.0, 1.0)

    case Overlay.LinearGradiant(fromPoint, _, toPoint, _) =>
      Array(
        fromPoint.x.toDouble,
        fromPoint.y.toDouble,
        toPoint.x.toDouble,
        toPoint.y.toDouble
      )
  }

  private val overlayToFromColorArray: Overlay => Array[Double] = {
    case Overlay.Color(color) =>
      color.toArray

    case Overlay.LinearGradiant(_, fromColor, _, _) =>
      fromColor.toArray
  }

  private val overlayToToColorArray: Overlay => Array[Double] = {
    case Overlay.Color(color) =>
      color.toArray

    case Overlay.LinearGradiant(_, _, _, toColor) =>
      toColor.toArray
  }

  private val thicknessToDouble: Thickness => Double = {
    case Thickness.None  => 0.0
    case Thickness.Thin  => 1.0
    case Thickness.Thick => 2.0
    case _               => 0.0
  }

  def fromEffects(effects: Effects): DisplayEffects =
    new DisplayEffects(
      effects.tint.toArray,
      overlayToPositionsArray(effects.overlay),
      overlayToFromColorArray(effects.overlay),
      overlayToToColorArray(effects.overlay),
      effects.border.color.toArray,
      thicknessToDouble(effects.border.innerThickness),
      thicknessToDouble(effects.border.outerThickness),
      effects.glow.color.toArray,
      effects.glow.innerGlowAmount,
      effects.glow.outerGlowAmount,
      effects.alpha,
      if (effects.flip.horizontal) -1 else 1,
      if (effects.flip.vertical) 1 else -1
    )

  val default: DisplayEffects =
    DisplayEffects.fromEffects(Effects.default)

}
