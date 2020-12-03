package indigo.shared.datatypes

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.time.Seconds

final case class Radians(value: Double) extends AnyVal {

  def +(other: Radians): Radians =
    Radians(this.value + other.value)

  def -(other: Radians): Radians =
    Radians(this.value - other.value)

  def *(other: Radians): Radians =
    Radians(this.value * other.value)

  def /(other: Radians): Radians =
    Radians(this.value / other.value)

  def hash: String =
    value.toString()

  def ===(other: Radians): Boolean =
    implicitly[EqualTo[Radians]].equal(this, other)

}
object Radians {

  implicit val equalTo: EqualTo[Radians] = {
    val eqD = implicitly[EqualTo[Double]]
    EqualTo.create { (a, b) =>
      eqD.equal(a.value, b.value)
    }
  }

  val `2PI`: Radians  = Radians(Math.PI * 2)
  val PI: Radians     = Radians(Math.PI)
  val PIby2: Radians  = Radians(Math.PI / 2)
  val TAU: Radians    = `2PI`
  val TAUby2: Radians = PI
  val TAUby4: Radians = PIby2

  def zero: Radians =
    Radians(0)

  def fromDegrees(degrees: Double): Radians =
    Radians((TAU.value / 360d) * (degrees % 360d))

  def fromSeconds(seconds: Seconds): Radians =
    Radians(TAU.value * (seconds.value % 1.0d))

}
