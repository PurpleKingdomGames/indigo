import indigo.macroshaders.ShaderDSL.*

// IndigoFrag(_ => rgba(1.0f, 1.0f, 0.0f, 1.0f)).toGLSL

trait Env:
  def UV: vec2

inline def circleSdf(p: vec2, r: Float): Float =
  length(p) - r

val frag =
  for {
    sdf  <- Fragment((env: Env) => circleSdf(env.UV - 0.5, 0.5))
    fill <- Fragment((env: Env) => rgba(env.UV, 0.0f, 1.0f))
    fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
  } yield rgba(fill.rgb * fillAmount, fillAmount)

// frag.toGLSL
