package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import com.purplekingdomgames.indigo.renderer.ClearColor

case class AmbientLight(tint: Tint, amount: Double)

object AmbientLight {
  val none: AmbientLight = AmbientLight(Tint.none, 1)

  implicit def ambientToClearColor(a: AmbientLight): ClearColor =
    ClearColor(a.tint.r * a.amount, a.tint.g * a.amount, a.tint.b * a.amount, 1)
}
