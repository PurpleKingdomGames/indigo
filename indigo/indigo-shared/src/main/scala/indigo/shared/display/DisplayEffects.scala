package indigo.shared.display

import indigo.shared.datatypes.Effects
import indigo.shared.datatypes.Overlay
import indigo.shared.datatypes.Thickness

final class DisplayEffects(
    val tint: Array[Float],
    val gradiantOverlayPositions: Array[Float],
    val gradiantOverlayFromColor: Array[Float],
    val gradiantOverlayToColor: Array[Float],
    val borderColor: Array[Float],
    val innerBorderAmount: Float,
    val outerBorderAmount: Float,
    val glowColor: Array[Float],
    val innerGlowAmount: Float,
    val outerGlowAmount: Float,
    val alpha: Float
)

object DisplayEffects {

  private val overlayToPositionsArray: Overlay => Array[Float] = {
    case Overlay.Color(_) =>
      Array(0.0f, 0.0f, 1.0f, 1.0f)

    case Overlay.LinearGradiant(fromPoint, _, toPoint, _) =>
      Array(
        fromPoint.x.toFloat,
        fromPoint.y.toFloat,
        toPoint.x.toFloat,
        toPoint.y.toFloat
      )

    case Overlay.RadialGradiant(fromPoint, _, toPoint, _) =>
      Array(
        fromPoint.x.toFloat,
        fromPoint.y.toFloat,
        toPoint.x.toFloat,
        toPoint.y.toFloat
      )
  }

  private val overlayToFromColorArray: Overlay => Array[Float] = {
    case Overlay.Color(color) =>
      color.toArray

    case Overlay.LinearGradiant(_, fromColor, _, _) =>
      fromColor.toArray

    case Overlay.RadialGradiant(_, fromColor, _, _) =>
      fromColor.toArray
  }

  private val overlayToToColorArray: Overlay => Array[Float] = {
    case Overlay.Color(color) =>
      color.toArray

    case Overlay.LinearGradiant(_, _, _, toColor) =>
      toColor.toArray

    case Overlay.RadialGradiant(_, _, _, toColor) =>
      toColor.toArray
  }

  private val thicknessToFloat: Thickness => Float = {
    case Thickness.None  => 0.0f
    case Thickness.Thin  => 1.0f
    case Thickness.Thick => 2.0f
  }

  def fromEffects(effects: Effects): DisplayEffects =
    new DisplayEffects(
      effects.tint.toArray,
      overlayToPositionsArray(effects.overlay),
      overlayToFromColorArray(effects.overlay),
      overlayToToColorArray(effects.overlay),
      effects.border.color.toArray,
      thicknessToFloat(effects.border.innerThickness),
      thicknessToFloat(effects.border.outerThickness),
      effects.glow.color.toArray,
      effects.glow.innerGlowAmount.toFloat,
      effects.glow.outerGlowAmount.toFloat,
      effects.alpha.toFloat
    )

  val default: DisplayEffects =
    DisplayEffects.fromEffects(Effects.default)

}
