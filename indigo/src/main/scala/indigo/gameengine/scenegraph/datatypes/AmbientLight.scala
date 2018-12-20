package indigo.gameengine.scenegraph.datatypes

import indigo.shared.ClearColor

final case class AmbientLight(tint: Tint, amount: Double) {
  def +(other: AmbientLight): AmbientLight =
    AmbientLight.combine(this, other)

  def withAmount(value: Double): AmbientLight =
    this.copy(amount = value)

  def withTint(r: Double, g: Double, b: Double): AmbientLight =
    this.copy(tint = Tint(r, g, b))

}

object AmbientLight {
  val Normal: AmbientLight = AmbientLight(Tint.None, 1)

  def combine(a: AmbientLight, b: AmbientLight): AmbientLight =
    (a, b) match {
      case (AmbientLight.Normal, x) =>
        x
      case (x, AmbientLight.Normal) =>
        x
      case (x, y) =>
        AmbientLight(x.tint + y.tint, x.amount + y.amount)
    }

  def toClearColor(a: AmbientLight): ClearColor =
    ClearColor(a.tint.r * a.amount, a.tint.g * a.amount, a.tint.b * a.amount, 1)
}
