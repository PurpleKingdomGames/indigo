package indigo.macroshaders

trait ShaderDSLOps extends ShaderDSLTypeExtensions:

  def length(genType: Float | vec2 | vec3 | vec4 | rgba): Float =
    genType match
      case f: Float =>
        Math.sqrt(Math.pow(f, 2.0f)).toFloat

      case vec2(x, y) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f)).toFloat

      case vec3(x, y, z) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f) + Math.pow(z, 2.0f)).toFloat

      case vec4(x, y, z, w) =>
        Math
          .sqrt(
            Math.pow(x, 2.0f) +
              Math.pow(y, 2.0f) +
              Math.pow(z, 2.0f) +
              Math.pow(w, 2.0f)
          )
          .toFloat
      case c: rgba =>
        length(c.toVec4)

  def step(edge: Float, x: Float): Float =
    if x < edge then 0.0f else 1.0f
