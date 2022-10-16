package indigo.macroshaders

trait ShaderDSLOps extends ShaderDSLTypeExtensions:

  inline def length(genType: Float): Float =
    Math.sqrt(Math.pow(genType, 2.0f)).toFloat
  inline def length(genType: vec2): Float =
    Math.sqrt(Math.pow(genType.x, 2.0f) + Math.pow(genType.y, 2.0f)).toFloat
  inline def length(genType: vec3): Float =
    Math.sqrt(Math.pow(genType.x, 2.0f) + Math.pow(genType.y, 2.0f) + Math.pow(genType.z, 2.0f)).toFloat
  inline def length(genType: vec4): Float =
    Math
      .sqrt(
        Math.pow(genType.x, 2.0f) +
          Math.pow(genType.y, 2.0f) +
          Math.pow(genType.z, 2.0f) +
          Math.pow(genType.w, 2.0f)
      )
      .toFloat

  inline def step(edge: Float, x: Float): Float =
    if x < edge then 0.0f else 1.0f
