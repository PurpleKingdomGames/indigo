package indigo.shared.datatypes

import annotation.targetName

import indigo.shared.time.Seconds

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

  inline def fromDegrees(degrees: Double): Radians =
    degrees % 360d * piBy180

  inline def fromSeconds(seconds: Seconds): Radians =
    pi2 * (seconds.toDouble % 1.0d)

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

    inline def wrap: Radians =
      ((r % pi2) + pi2) % pi2

    inline def negative: Radians =
      -r

    inline def invert: Radians =
      negative

    inline def toDouble: Double =
      r

    inline def toFloat: Float =
      r.toFloat
