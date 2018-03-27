package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import com.purplekingdomgames.shared.ClearColor

case class AmbientLight(tint: Tint, amount: Double) {
  def +(other: AmbientLight): AmbientLight =
    AmbientLight.combine(this, other)
}

object AmbientLight {
  val None: AmbientLight = AmbientLight(Tint.None, 1)

  def combine(a: AmbientLight, b: AmbientLight): AmbientLight =
    (a, b) match {
      case (AmbientLight.None, x) =>
        x
      case (x, AmbientLight.None) =>
        x
      case (x, y) =>
        AmbientLight(x.tint + y.tint, x.amount + y.amount)
    }

  implicit def ambientToClearColor(a: AmbientLight): ClearColor =
    ClearColor(a.tint.r * a.amount, a.tint.g * a.amount, a.tint.b * a.amount, 1)
}
