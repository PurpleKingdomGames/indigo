package indigo.shared.materials

import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.ShaderPrimitive.vec4
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Fill
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.datatypes.RGB

trait BlendMaterial {
  def toShaderData: BlendShaderData
}

object BlendMaterial {

  case object Normal extends BlendMaterial {
    def toShaderData: BlendShaderData =
      BlendShaderData(
        StandardShaders.NormalBlend.id,
        Nil
      )
  }

  final case class Lighting(ambient: RGBA) extends BlendMaterial {
    def toShaderData: BlendShaderData =
      BlendShaderData(
        StandardShaders.LightingBlend.id,
        List(
          UniformBlock(
            "IndigoLightingBlendData",
            List(
              Uniform("AMBIENT_LIGHT_COLOR") -> vec4(ambient.r, ambient.g, ambient.b, ambient.a)
            )
          )
        )
      )
  }

  final case class BlendEffects(alpha: Double, tint: RGBA, overlay: Fill, saturation: Double, affectsBackground: Boolean) extends BlendMaterial {

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

      BlendShaderData(
        StandardShaders.BlendEffects.id,
        List(
          UniformBlock(
            "IndigoBlendEffectsData",
            List(
              Uniform("ALPHA_SATURATION_OVERLAYTYPE_BG") -> vec4(alpha, saturation, overlayType, if (affectsBackground) 1.0 else 0.0),
              Uniform("TINT")                            -> vec4(tint.r, tint.g, tint.b, tint.a)
            ) ++ gradientUniforms
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
