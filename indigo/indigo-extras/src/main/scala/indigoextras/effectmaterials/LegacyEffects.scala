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

final case class LegacyEffects(diffuse: AssetName, alpha: Double, tint: RGBA, overlay: Fill, saturation: Double) extends Material {

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
            Uniform("GRADIENT_FROM_TO")    -> vec4(fromPoint.x.toDouble, fromPoint.y.toDouble, toPoint.x.toDouble, toPoint.y.toDouble),
            Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
            Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
          )

        case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
          List(
            Uniform("GRADIENT_FROM_TO")    -> vec4(fromPoint.x.toDouble, fromPoint.y.toDouble, toPoint.x.toDouble, toPoint.y.toDouble),
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
      Some(
        UniformBlock(
          "IndigoLegacyEffectsData",
          List(
            Uniform("ALPHA_SATURATION_OVERLAYTYPE") -> vec3(alpha, saturation, overlayType),
            Uniform("TINT")                         -> vec4(tint.r, tint.g, tint.b, tint.a)
          ) ++ gradientUniforms ++
            List(
              Uniform("BORDER_COLOR") -> vec4(tint.r, tint.g, tint.b, tint.a), // TODO
              Uniform("GLOW_COLOR") -> vec4(tint.r, tint.g, tint.b, tint.a), // TODO
              Uniform("EFFECT_AMOUNTS") -> vec4(tint.r, tint.g, tint.b, tint.a) // TODO // outer border, inner border, outer glow, inner glow
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
      light = ShaderLibrary.NoOpLight
    )

  def apply(diffuse: AssetName): LegacyEffects =
    LegacyEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0)

  def apply(diffuse: AssetName, alpha: Double): LegacyEffects =
    LegacyEffects(diffuse, alpha, RGBA.None, Fill.Color.default, 1.0)
}
