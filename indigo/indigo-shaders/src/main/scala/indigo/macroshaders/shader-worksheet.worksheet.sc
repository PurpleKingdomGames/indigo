import indigo.macroshaders.ShaderDSL.*
import indigo.macroshaders.ShaderGroup
import indigo.macroshaders.Shader
import indigo.macroshaders.Program
import indigo.macroshaders.Pipeline
import indigo.macroshaders.ShaderId
import indigo.macroshaders.ShaderMacros

trait VertEnv
trait FragEnv:
  def UV: vec2

val vertex: Shader[VertEnv, vec4] =
  Shader(_ => Program(vec4(1.0)))

// vertex.toGLSL

//---

inline def frag: Shader[FragEnv, rgba] =
  Shader { env =>
    val uv = env.UV
    val alpha = 1.0f
    Program(rgba(uv, 1.0f, alpha))
  }

ShaderMacros.toAST(frag).render

inline def circleSdf(p: vec2, r: Float): Program[Float] =
  Program(length(p) - r)

inline def calculateColour(uv: vec2, sdf: Float): Program[rgba] =
  Program {
    val fill       = rgba(uv, 0.0f, 1.0f)
    val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
    rgba(fill.rgb * fillAmount, fillAmount)
  }

val frag2: Shader[FragEnv, rgba] =
  Shader { env =>
    for {
      sdf    <- circleSdf(env.UV - 0.5f, 0.5f)
      colour <- calculateColour(env.UV, sdf)
    } yield colour
  }

val shaderV1 = ShaderGroup(ShaderId("circle"), vertex, frag)

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

val frag3: Shader[FragEnv, rgba] =
  Shader { env =>
    for {
      sdf    <- Program(env.UV) |> (shiftUV >>> toSdf(0.5))
      colour <- Program(env.UV) |> toOutColor(sdf)
    } yield colour
  }

val shaderV2 = ShaderGroup(ShaderId("circle"), vertex, frag3)

