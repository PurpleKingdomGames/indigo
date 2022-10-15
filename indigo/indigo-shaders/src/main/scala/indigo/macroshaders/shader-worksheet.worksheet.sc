import indigo.macroshaders.ShaderDSL.*
import indigo.macroshaders.ShaderContext
import indigo.macroshaders.Shader
import indigo.macroshaders.Program
import indigo.macroshaders.Pipeline
import indigo.macroshaders.ShaderId

IndigoFrag(_ => rgba(1.0f, 1.0f, 0.0f, 1.0f)).toGLSL

trait VertEnv
trait FragEnv:
  def UV: vec2

val vertex: ShaderContext[VertEnv, Program[vec4]] =
  ShaderContext(_ => Program(vec4(1.0)))

//---

inline def circleSdf(p: vec2, r: Float): Program[Float] =
  Program(length(p) - r)

inline def calculateColour(uv: vec2, sdf: Float): Program[rgba] =
  Program {
    val fill       = rgba(uv, 0.0f, 1.0f)
    val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
    rgba(fill.rgb * fillAmount, fillAmount)
  }

val frag: ShaderContext[FragEnv, Program[rgba]] =
  ShaderContext { env =>
    for {
      sdf    <- circleSdf(env.UV - 0.5f, 0.5f)
      colour <- calculateColour(env.UV, sdf)
    } yield colour
  }

val shaderV1 = Shader(ShaderId("circle"), vertex, frag)

//---

inline def shiftUV: Pipeline[vec2, vec2] =
  Pipeline(_ - 0.5f)

inline def toSdf(r: Float): Pipeline[vec2, Float] =
  Pipeline(p => length(p) - r)

inline def toOutColor(sdf: Float): Pipeline[vec2, rgba] =
  Pipeline { uv =>
    val fill       = rgba(uv, 0.0f, 1.0f)
    val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
    rgba(fill.rgb * fillAmount, fillAmount)
  }

val altfrag: ShaderContext[FragEnv, Program[rgba]] =
  ShaderContext { env =>
    for {
      sdf    <- Program(env.UV) |> (shiftUV >>> toSdf(0.5))
      colour <- Program(env.UV) |> toOutColor(sdf)
    } yield colour
  }

val shaderV2 = Shader(ShaderId("circle"), vertex, altfrag)
