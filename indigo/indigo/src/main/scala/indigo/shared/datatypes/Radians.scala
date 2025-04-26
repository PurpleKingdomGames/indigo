package indigo.shared.datatypes

import indigo.shared.dice.Dice
import indigo.shared.time.Seconds

import scala.math

import annotation.targetName

opaque type Radians = Double
object Radians:

  inline private val pi      = Math.PI
  inline private val pi2     = Math.PI * 2
  inline private val piBy180 = Math.PI / 180d

  inline def apply(radians: Double): Radians = radians

  val `2PI`: Radians  = Radians(pi2)
  val PI: Radians     = Radians(pi)
  val PIby2: Radians  = Radians(pi / 2)
  val TAU: Radians    = `2PI`
  val TAUby2: Radians = PI
  val TAUby4: Radians = PIby2
  val zero: Radians   = Radians(0)

  /** Converts degrees to radians, allowing negative angles if input is negative.
    */
  inline def fromDegrees(degrees: Double): Radians =
    degrees % 360d * piBy180

  inline def fromSeconds(seconds: Seconds): Radians =
    pi2 * (seconds.toDouble % 1.0d)

  def mod(dividend: Radians, divisor: Radians): Radians =
    Radians((dividend % divisor + divisor) % divisor)

  def random(dice: Dice): Radians =
    TAU * dice.rollDouble

  extension (r: Radians)
    def +(other: Radians): Radians =
      Radians(r + other)
    @targetName("+_Double")
    def +(other: Double): Radians =
      Radians(r + other)

    def -(other: Radians): Radians =
      Radians(r - other)
    @targetName("-_Double")
    def -(other: Double): Radians =
      Radians(r - other)

    def *(other: Radians): Radians =
      Radians(r * other)
    @targetName("*_Double")
    def *(other: Double): Radians =
      Radians(r * other)

    def /(other: Radians): Radians =
      Radians(r / other)
    @targetName("/_Double")
    def /(other: Double): Radians =
      Radians(r / other)

    def %(other: Radians): Radians =
      Radians.mod(r, other)
    @targetName("%_Double")
    def %(other: Double): Radians =
      Radians.mod(r, other)

    def wrap: Radians =
      val m = r % pi2
      Radians(if m < 0 then m + pi2 else m)

    def centeredWrap: Radians =
      val w = (r + pi) % pi2
      Radians(if w < 0 then w + pi2 else w) - PI

    def negative: Radians =
      -r

    def invert: Radians =
      negative

    def `unary_-`: Radians = negative

    def ~==(other: Radians): Boolean =
      Math.abs(r.toDouble - other.toDouble) < 0.001

    def toDouble: Double =
      r

    def max(other: Radians): Radians = math.max(r, other)

    def min(other: Radians): Radians = math.min(r, other)

    def toFloat: Float =
      r.toFloat

    def toDegrees: Double =
      (360 / pi2) * r.toDouble
