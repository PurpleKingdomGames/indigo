package indigo.shared.materials

import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.ShaderPrimitive.rawJSArray
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock

trait BlendMaterial {
  def toShaderData: BlendShaderData
}

object BlendMaterial {

  case object Normal extends BlendMaterial derives CanEqual {
    def toShaderData: BlendShaderData =
      BlendShaderData(
        StandardShaders.NormalBlend.id,
        Nil
      )
  }

  final case class Lighting(ambient: RGBA) extends BlendMaterial derives CanEqual {
    def toShaderData: BlendShaderData =
      BlendShaderData(
        StandardShaders.LightingBlend.id,
        List(
          UniformBlock(
            "IndigoLightingBlendData",
            List(
              Uniform("AMBIENT_LIGHT_COLOR") -> rawJSArray(
                ambient.r.toFloat,
                ambient.g.toFloat,
                ambient.b.toFloat,
                ambient.a.toFloat
              )
            )
          )
        )
      )
  }

  final case class BlendEffects(
      alpha: Double,
      tint: RGBA,
      overlay: Fill,
      saturation: Double,
      affectsBackground: Boolean
  ) extends BlendMaterial
      derives CanEqual {

    def withAlpha(newAlpha: Double): BlendEffects =
      this.copy(alpha = newAlpha)

    def withTint(newTint: RGBA): BlendEffects =
      this.copy(tint = newTint)
    def withTint(newTint: RGB): BlendEffects =
      this.copy(tint = newTint.toRGBA)

    def withOverlay(newOverlay: Fill): BlendEffects =
      this.copy(overlay = newOverlay)

    def withSaturation(newSaturation: Double): BlendEffects =
      this.copy(saturation = newSaturation)

    def withAffectBackground(affectsBg: Boolean): BlendMaterial =
      this.copy(affectsBackground = affectsBg)
    def applyToBackground: BlendMaterial =
      this.copy(affectsBackground = true)
    def ignoreBackground: BlendMaterial =
      this.copy(affectsBackground = false)

    def toShaderData: BlendShaderData = {
      val overlayType: Float =
        overlay match {
          case _: Fill.Color          => 0.0
          case _: Fill.LinearGradient => 1.0
          case _: Fill.RadialGradient => 2.0
        }

      BlendShaderData(
        StandardShaders.BlendEffects.id,
        List(
          UniformBlock(
            "IndigoBlendEffectsData",
            List(
              // ALPHA_SATURATION_OVERLAYTYPE_BG (vec4), TINT (vec4)
              Uniform("BlendEffects_DATA") -> rawJSArray(
                scalajs.js.Array(
                  alpha.toFloat,
                  saturation.toFloat,
                  overlayType,
                  if (affectsBackground) 1.0f else 0.0f,
                  tint.r.toFloat,
                  tint.g.toFloat,
                  tint.b.toFloat,
                  tint.a.toFloat
                )
              )
            ) ++ overlay.toUniformData("BlendEffects")
          )
        )
      )
    }
  }
  object BlendEffects {
    val None: BlendEffects =
      BlendEffects(1.0, RGBA.None, Fill.Color.default, 1.0, false)

    def apply(alpha: Double): BlendEffects =
      BlendEffects(alpha, RGBA.None, Fill.Color.default, 1.0, false)
  }

}
