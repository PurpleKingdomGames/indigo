package indigo.macroshaders

trait ShaderDSLTypes:
  
  final case class vec2(x: Float, y: Float):
    def +(f: Float): vec2 = vec2(x + f, y + f)
    def -(f: Float): vec2 = vec2(x - f, y - f)
    def *(f: Float): vec2 = vec2(x * f, y * f)
    def /(f: Float): vec2 = vec2(x / f, y / f)

    def +(v: vec2): vec2 = vec2(x + v.x, y + v.y)
    def -(v: vec2): vec2 = vec2(x - v.x, y - v.y)
    def *(v: vec2): vec2 = vec2(x * v.x, y * v.y)
    def /(v: vec2): vec2 = vec2(x / v.x, y / v.y)
  object vec2:
    inline def apply(xy: Float): vec2 =
      vec2(xy, xy)

  final case class vec3(x: Float, y: Float, z: Float):
    def +(f: Float): vec3 = vec3(x + f, y + f, z + f)
    def -(f: Float): vec3 = vec3(x - f, y - f, z - f)
    def *(f: Float): vec3 = vec3(x * f, y * f, z * f)
    def /(f: Float): vec3 = vec3(x / f, y / f, z / f)

    def +(v: vec3): vec3 = vec3(x + v.x, y + v.y, z + v.z)
    def -(v: vec3): vec3 = vec3(x - v.x, y - v.y, z - v.z)
    def *(v: vec3): vec3 = vec3(x * v.x, y * v.y, z * v.z)
    def /(v: vec3): vec3 = vec3(x / v.x, y / v.y, z / v.z)
  object vec3:
    inline def apply(xyz: Float): vec3 =
      vec3(xyz, xyz, xyz)

    inline def apply(xy: vec2, z: Float): vec3 =
      vec3(xy.x, xy.y, z)

    inline def apply(x: Float, yz: vec2): vec3 =
      vec3(x, yz.x, yz.y)

  final case class vec4(x: Float, y: Float, z: Float, w: Float):
    def +(f: Float): vec4 = vec4(x + f, y + f, z + f, w + f)
    def -(f: Float): vec4 = vec4(x - f, y - f, z - f, w - f)
    def *(f: Float): vec4 = vec4(x * f, y * f, z * f, w * f)
    def /(f: Float): vec4 = vec4(x / f, y / f, z / f, w / f)

    def +(v: vec4): vec4 = vec4(x + v.x, y + v.y, z + v.z, w + v.w)
    def -(v: vec4): vec4 = vec4(x - v.x, y - v.y, z - v.z, w - v.w)
    def *(v: vec4): vec4 = vec4(x * v.x, y * v.y, z * v.z, w * v.w)
    def /(v: vec4): vec4 = vec4(x / v.x, y / v.y, z / v.z, w / v.w)

    def toRGBA: rgba = rgba(x, y, z, w)
  object vec4:
    inline def apply(xy: vec2, z: Float, w: Float): vec4 =
      vec4(xy.x, xy.y, z, w)

    inline def apply(x: Float, yz: vec2, w: Float): vec4 =
      vec4(x, yz.x, yz.y, w)

    inline def apply(x: Float, y: Float, zw: vec2): vec4 =
      vec4(x, y, zw.x, zw.y)

    inline def apply(xy: vec2, zw: vec2): vec4 =
      vec4(xy.x, xy.y, zw.x, zw.y)

    inline def apply(xyz: vec3, w: Float): vec4 =
      vec4(xyz.x, xyz.y, xyz.z, w)

    inline def apply(x: Float, yzw: vec3): vec4 =
      vec4(x, yzw.x, yzw.y, yzw.z)

    inline def apply(xyz: Float): vec4 =
      vec4(xyz, xyz, xyz, xyz)

  final case class rgba(r: Float, g: Float, b: Float, a: Float):
    def +(f: Float): rgba = rgba(r + f, g + f, b + f, a + f)
    def -(f: Float): rgba = rgba(r - f, g - f, b - f, a - f)
    def *(f: Float): rgba = rgba(r * f, g * f, b * f, a * f)
    def /(f: Float): rgba = rgba(r / f, g / f, b / f, a / f)

    def toVec4: vec4 = vec4(r, g, b, a)
  object rgba:
    inline def apply(rg: vec2, b: Float, a: Float): rgba =
      rgba(rg.x, rg.y, b, a)

    inline def apply(r: Float, gb: vec2, a: Float): rgba =
      rgba(r, gb.x, gb.y, a)

    inline def apply(r: Float, g: Float, ba: vec2): rgba =
      rgba(r, g, ba.x, ba.y)

    inline def apply(rg: vec2, ba: vec2): rgba =
      rgba(rg.x, rg.y, ba.x, ba.y)

    inline def apply(rgb: vec3, a: Float): rgba =
      rgba(rgb.x, rgb.y, rgb.z, a)

    inline def apply(r: Float, gba: vec3): rgba =
      rgba(r, gba.x, gba.y, gba.z)

    inline def apply(rgb: Float): rgba =
      rgba(rgb, rgb, rgb, rgb)
