package indigo.macroshaders

object ShaderDSL:

  final class vec2(val x: Float, val y: Float)
  object vec2:
    def apply(x: Float, y: Float): vec2 =
      new vec2(x, y)

    def apply(xy: Float): vec2 =
      new vec2(xy, xy)

  final class vec3(val x: Float, val y: Float, val z: Float)
  object vec3:
    def apply(x: Float, y: Float, z: Float): vec3 =
      new vec3(x, y, z)

    def apply(xyz: Float): vec3 =
      new vec3(xyz, xyz, xyz)

    def apply(x: Float, yz: vec2): vec3 =
      new vec3(x, yz.x, yz.y)

    def apply(xy: vec2, z: Float): vec3 =
      new vec3(xy.x, xy.y, z)

  final class vec4(val x: Float, val y: Float, val z: Float, val w: Float)
  object vec4:
    def apply(x: Float, y: Float, z: Float, w: Float): vec4 =
      new vec4(x, y, z, w)

    def apply(xyz: Float): vec4 =
      new vec4(xyz, xyz, xyz, xyz)

    def apply(xy: vec2, zw: vec2): vec4 =
      new vec4(xy.x, xy.y, zw.x, zw.y)

    def apply(x: Float, y: Float, zw: vec2): vec4 =
      new vec4(x, y, zw.x, zw.y)

    def apply(xy: vec2, z: Float, w: Float): vec4 =
      new vec4(xy.x, xy.y, z, w)

    def apply(x: Float, yzw: vec3): vec4 =
      new vec4(x, yzw.x, yzw.y, yzw.z)

    def apply(xyz: vec3, w: Float): vec4 =
      new vec4(xyz.x, xyz.y, xyz.z, w)

  final class rgba(val r: Float, val g: Float, val b: Float, val a: Float)
  object rgba:
    def apply(r: Float, g: Float, b: Float, a: Float): rgba =
      new rgba(r, g, b, a)

    def apply(rgb: Float): rgba =
      new rgba(rgb, rgb, rgb, rgb)

    def apply(rg: vec2, ba: vec2): rgba =
      new rgba(rg.x, rg.y, ba.x, ba.y)

    def apply(r: Float, g: Float, ba: vec2): rgba =
      new rgba(r, g, ba.x, ba.y)

    def apply(rg: vec2, b: Float, a: Float): rgba =
      new rgba(rg.x, rg.y, b, a)

    def apply(r: Float, gba: vec3): rgba =
      new rgba(r, gba.x, gba.y, gba.z)

    def apply(rgb: vec3, a: Float): rgba =
      new rgba(rgb.x, rgb.y, rgb.z, a)

    def apply(v4: vec4): rgba =
      new rgba(v4.x, v4.y, v4.z, v4.y)

end ShaderDSL
