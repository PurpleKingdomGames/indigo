package indigo.shared.display

import indigo.shared.datatypes.Effects
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.Thickness

final class DisplayEffects(
    val tint: Array[Float],
    val gradientOverlayPositions: Array[Float],
    val gradientOverlayFromColor: Array[Float],
    val gradientOverlayToColor: Array[Float],
    val borderColor: Array[Float],
    val innerBorderAmount: Float,
    val outerBorderAmount: Float,
    val glowColor: Array[Float],
    val innerGlowAmount: Float,
    val outerGlowAmount: Float,
    val alpha: Float
)

object DisplayEffects {

  private val overlayToPositionsArray: Fill => Array[Float] = {
    case Fill.Color(_) =>
      Array(0.0f, 0.0f, 1.0f, 1.0f)

    case Fill.LinearGradient(fromPoint, _, toPoint, _) =>
      Array(
        fromPoint.x.toFloat,
        fromPoint.y.toFloat,
        toPoint.x.toFloat,
        toPoint.y.toFloat
      )

    case Fill.RadialGradient(fromPoint, _, toPoint, _) =>
      Array(
        fromPoint.x.toFloat,
        fromPoint.y.toFloat,
        toPoint.x.toFloat,
        toPoint.y.toFloat
      )
  }

  private val overlayToFromColorArray: Fill => Array[Float] = {
    case Fill.Color(color) =>
      color.toArray

    case Fill.LinearGradient(_, fromColor, _, _) =>
      fromColor.toArray

    case Fill.RadialGradient(_, fromColor, _, _) =>
      fromColor.toArray
  }

  private val overlayToToColorArray: Fill => Array[Float] = {
    case Fill.Color(color) =>
      color.toArray

    case Fill.LinearGradient(_, _, _, toColor) =>
      toColor.toArray

    case Fill.RadialGradient(_, _, _, toColor) =>
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
