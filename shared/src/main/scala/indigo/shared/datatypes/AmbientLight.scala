package indigo.shared.datatypes

import indigo.shared.AsString
import indigo.shared.EqualTo

import indigo.shared.ClearColor

final class AmbientLight(val tint: Tint, val amount: Double) {
  def +(other: AmbientLight): AmbientLight =
    AmbientLight.combine(this, other)

  def withAmount(value: Double): AmbientLight =
    AmbientLight(tint, value)

  def withTint(r: Double, g: Double, b: Double): AmbientLight =
    AmbientLight(Tint(r, g, b), amount)

}

object AmbientLight {

  implicit val show: AsString[AmbientLight] = {
    val st = implicitly[AsString[Tint]]
    val sd = implicitly[AsString[Double]]

    AsString.create { v =>
      s"AmbientLight(${st.show(v.tint)}, ${sd.show(v.amount)})"
    }
  }

  implicit val eq: EqualTo[AmbientLight] = {
    val et = implicitly[EqualTo[Tint]]
    val ed = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      et.equal(a.tint, b.tint) && ed.equal(a.amount, b.amount)
    }
  }

  def apply(tint: Tint, amount: Double): AmbientLight =
    new AmbientLight(tint, amount)

  val Normal: AmbientLight = AmbientLight(Tint.None, 1)
  val None: AmbientLight = AmbientLight(Tint.None, 0)

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
