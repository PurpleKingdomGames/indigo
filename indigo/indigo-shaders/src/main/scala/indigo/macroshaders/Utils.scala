package indigo.macroshaders

import ShaderDSL.*

object Utils:

  def hash(x: vec2): vec2 =
    val k  = vec2(0.3183099, 0.3678794)
    val xx = x * k + k.yx
    -1.0f + 2.0f * fract(16.0f * k * fract(xx.x * xx.y * (xx.x + xx.y)))

  def calcNoise(p: vec2): vec3 =
    val i: vec2  = floor(p)
    val f: vec2  = fract(p)
    val u: vec2  = f * f * (3.0f - 2.0f * f)
    val du: vec2 = 6.0f * f * (1.0f - f)
    val ga: vec2 = hash(i + vec2(0.0, 0.0))
    val gb: vec2 = hash(i + vec2(1.0, 0.0))
    val gc: vec2 = hash(i + vec2(0.0, 1.0))
    val gd: vec2 = hash(i + vec2(1.0, 1.0))

    val va: Float = dot(ga, f - vec2(0.0, 0.0))
    val vb: Float = dot(gb, f - vec2(1.0, 0.0))
    val vc: Float = dot(gc, f - vec2(0.0, 1.0))
    val vd: Float = dot(gd, f - vec2(1.0, 1.0))

    vec3(
      va + u.x * (vb - va) + u.y * (vc - va) + u.x * u.y * (va - vb - vc + vd),
      ga + u.x * (gb - ga) + u.y * (gc - ga) + u.x * u.y * (ga - gb - gc + gd) +
        du * (u.yx * (va - vb - vc + vd) + vec2(vb, vc) - va)
    )
