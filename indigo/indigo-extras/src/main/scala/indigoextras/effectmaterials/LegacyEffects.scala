package indigoextras.effectmaterials

import indigo.shared.assets.AssetName
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigoextras.shaders.ExtrasShaderLibrary
import indigo.shared.shader.ShaderPrimitive.{vec3, vec4}
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.Uniform
import indigo.shared.shader.EntityShader
import indigo.shaders.ShaderLibrary
import indigo.shared.shader.ShaderId
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.RGB

final case class LegacyEffects(
    diffuse: AssetName,
    alpha: Double,
    tint: RGBA,
    overlay: Fill,
    saturation: Double,
    border: Border,
    glow: Glow
) extends Material derives CanEqual {

  def withAlpha(newAlpha: Double): LegacyEffects =
    this.copy(alpha = newAlpha)

  def withTint(newTint: RGBA): LegacyEffects =
    this.copy(tint = newTint)
  def withTint(newTint: RGB): LegacyEffects =
    this.copy(tint = newTint.toRGBA)

  def withOverlay(newOverlay: Fill): LegacyEffects =
    this.copy(overlay = newOverlay)

  def withSaturation(newSaturation: Double): LegacyEffects =
    this.copy(saturation = newSaturation)

  def withBorder(newBorder: Border): LegacyEffects =
    this.copy(border = newBorder)

  def withGlow(newGlow: Glow): LegacyEffects =
    this.copy(glow = newGlow)

  def toShaderData: ShaderData = {
    val gradientUniforms: List[(Uniform, ShaderPrimitive)] =
      overlay match {
        case Fill.Color(color) =>
          val c = vec4(color.r, color.g, color.b, color.a)
          List(
            Uniform("GRADIENT_FROM_TO")    -> vec4(0.0d),
            Uniform("GRADIENT_FROM_COLOR") -> c,
            Uniform("GRADIENT_TO_COLOR")   -> c
          )

        case Fill.LinearGradient(fromPoint, fromColor, toPoint, toColor) =>
          List(
            Uniform("GRADIENT_FROM_TO") -> vec4(
              fromPoint.x.toDouble,
              fromPoint.y.toDouble,
              toPoint.x.toDouble,
              toPoint.y.toDouble
            ),
            Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
            Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
          )

        case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
          List(
            Uniform("GRADIENT_FROM_TO") -> vec4(
              fromPoint.x.toDouble,
              fromPoint.y.toDouble,
              toPoint.x.toDouble,
              toPoint.y.toDouble
            ),
            Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
            Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
          )
      }

    val overlayType: Double =
      overlay match {
        case _: Fill.Color          => 0.0
        case _: Fill.LinearGradient => 1.0
        case _: Fill.RadialGradient => 2.0
      }

    ShaderData(
      LegacyEffects.entityShader.id,
      List(
        UniformBlock(
          "IndigoLegacyEffectsData",
          List(
            Uniform("ALPHA_SATURATION_OVERLAYTYPE") -> vec3(alpha, saturation, overlayType),
            Uniform("TINT")                         -> vec4(tint.r, tint.g, tint.b, tint.a)
          ) ++ gradientUniforms ++
            List(
              Uniform("BORDER_COLOR") -> vec4(border.color.r, border.color.g, border.color.b, border.color.a),
              Uniform("GLOW_COLOR")   -> vec4(glow.color.r, glow.color.g, glow.color.b, glow.color.a),
              Uniform("EFFECT_AMOUNTS") -> vec4(
                border.outerThickness.toInt.toDouble,
                border.innerThickness.toInt.toDouble,
                glow.outerGlowAmount,
                glow.innerGlowAmount
              )
            )
        )
      ),
      Some(diffuse),
      None,
      None,
      None
    )
  }
}
object LegacyEffects {

  val entityShader: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigoextras_engine_legacy_effects]"),
      vertex = ExtrasShaderLibrary.LegacyEffectsVertex,
      fragment = ExtrasShaderLibrary.LegacyEffectsFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  def apply(diffuse: AssetName): LegacyEffects =
    LegacyEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0, Border.default, Glow.default)

  def apply(diffuse: AssetName, alpha: Double): LegacyEffects =
    LegacyEffects(diffuse, alpha, RGBA.None, Fill.Color.default, 1.0, Border.default, Glow.default)
}

final case class Border(color: RGBA, innerThickness: Thickness, outerThickness: Thickness) derives CanEqual {

  def withColor(newColor: RGBA): Border =
    this.copy(color = newColor)

  def withInnerThickness(thickness: Thickness): Border =
    this.copy(innerThickness = thickness)

  def withOuterThickness(thickness: Thickness): Border =
    this.copy(outerThickness = thickness)

  def hash: String =
    color.hash + innerThickness.hash + outerThickness.hash
}
object Border {
  def inside(color: RGBA): Border =
    Border(color, Thickness.Thin, Thickness.None)

  def outside(color: RGBA): Border =
    Border(color, Thickness.None, Thickness.Thin)

  val default: Border =
    Border(RGBA.Zero, Thickness.None, Thickness.None)
}

enum Thickness derives CanEqual:
  case None, Thin, Thick

object Thickness:
  extension (t: Thickness)
    def toInt: Int =
      t match
        case Thickness.None  => 0
        case Thickness.Thin  => 1
        case Thickness.Thick => 2

    def hash: String =
      t.toInt.toString()

final case class Glow(color: RGBA, innerGlowAmount: Double, outerGlowAmount: Double) derives CanEqual {
  def withColor(newColor: RGBA): Glow =
    this.copy(color = newColor)

  def withInnerGlowAmount(amount: Double): Glow =
    this.copy(innerGlowAmount = Math.max(0, amount))

  def withOuterGlowAmount(amount: Double): Glow =
    this.copy(outerGlowAmount = Math.max(0, amount))

  def hash: String =
    color.hash + innerGlowAmount.toString + outerGlowAmount.toString()
}
object Glow {
  def inside(color: RGBA): Glow =
    Glow(color, 1d, 0d)

  def outside(color: RGBA): Glow =
    Glow(color, 0d, 1d)

  val default: Glow =
    Glow(RGBA.Zero, 0d, 0d)
}
