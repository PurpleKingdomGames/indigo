package indigo.macroshaders

object ShaderDSL:

  // Structure
  final case class Uniform[T](name: String)
  final case class UniformBlock(uniforms: List[Uniform[_]])

  final case class ShaderEnv(uniformBlocks: List[UniformBlock])

  opaque type IndigoFrag = Function1[ShaderEnv, rgba]
  object IndigoFrag:
    inline def apply(f: ShaderEnv => rgba): IndigoFrag = f

    extension (inline frag: IndigoFrag)
      inline def toGLSL: String =
        ShaderMacros.toAST(frag).render

  // Operations

  // Primitives

  extension (f: Float) def toGLSL: String = s"""float(${f.toString})"""

  sealed trait glsl
  final case class vec2(x: Float, y: Float)
  object vec2:
    inline def apply(x: Float, y: Float): vec2 =
      vec2(x, y)
    inline def apply(xy: Float): vec2 =
      vec2(xy, xy)

    extension (v: vec2) def toGLSL: String = s"""vec2(${v.x}, ${v.y})"""

  final case class vec3(x: Float, y: Float, z: Float)
  object vec3:

    inline def apply(xyz: Float): vec3 =
      vec3(xyz, xyz, xyz)

    inline def apply(x: Float, yz: vec2): vec3 =
      vec3(x, yz.x, yz.y)

    inline def apply(xy: vec2, z: Float): vec3 =
      vec3(xy.x, xy.y, z)

    extension (v: vec3) def toGLSL: String = s"""vec3(${v.x}, ${v.y}, ${v.z})"""

  final case class vec4(x: Float, y: Float, z: Float, w: Float)
  object vec4:

    inline def apply(xyz: Float): vec4 =
      vec4(xyz, xyz, xyz, xyz)

    inline def apply(xy: vec2, zw: vec2): vec4 =
      vec4(xy.x, xy.y, zw.x, zw.y)

    inline def apply(x: Float, y: Float, zw: vec2): vec4 =
      vec4(x, y, zw.x, zw.y)

    inline def apply(xy: vec2, z: Float, w: Float): vec4 =
      vec4(xy.x, xy.y, z, w)

    inline def apply(x: Float, yzw: vec3): vec4 =
      vec4(x, yzw.x, yzw.y, yzw.z)

    inline def apply(xyz: vec3, w: Float): vec4 =
      vec4(xyz.x, xyz.y, xyz.z, w)

    extension (v: vec4) def toGLSL: String = s"""vec4(${v.x}, ${v.y}, ${v.z}, ${v.w})"""

  final case class rgba(r: Float, g: Float, b: Float, a: Float)
  object rgba:

    inline def apply(rgb: Float): rgba =
      rgba(rgb, rgb, rgb, rgb)

    inline def apply(rg: vec2, ba: vec2): rgba =
      rgba(rg.x, rg.y, ba.x, ba.y)

    inline def apply(r: Float, g: Float, ba: vec2): rgba =
      rgba(r, g, ba.x, ba.y)

    inline def apply(rg: vec2, b: Float, a: Float): rgba =
      rgba(rg.x, rg.y, b, a)

    inline def apply(r: Float, gba: vec3): rgba =
      rgba(r, gba.x, gba.y, gba.z)

    inline def apply(rgb: vec3, a: Float): rgba =
      rgba(rgb.x, rgb.y, rgb.z, a)

    inline def apply(v4: vec4): rgba =
      rgba(v4.x, v4.y, v4.z, v4.y)

    extension (c: rgba) def toGLSL: String = s"""vec4(${c.r}, ${c.g}, ${c.b}, ${c.a})"""

end ShaderDSL
