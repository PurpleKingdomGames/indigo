package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

// Graphical effects
object Effects {
  val default = Effects(
    alpha = 1.0,
    tint = Tint(
      r = 1,
      g = 1,
      b = 1
    ),
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )
}
case class Effects(alpha: Double, tint: Tint, flip: Flip)
case class Tint(r: Double, g: Double, b: Double)
case class Flip(horizontal: Boolean, vertical: Boolean)