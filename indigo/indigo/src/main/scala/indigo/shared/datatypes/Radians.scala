package indigo.shared.datatypes

import annotation.targetName

import indigo.shared.time.Seconds

opaque type Radians = Double
object Radians:

  def apply(radians: Double): Radians = radians

  val `2PI`: Radians  = Radians(Math.PI * 2)
  val PI: Radians     = Radians(Math.PI)
  val PIby2: Radians  = Radians(Math.PI / 2)
  val TAU: Radians    = `2PI`
  val TAUby2: Radians = PI
  val TAUby4: Radians = PIby2

  def zero: Radians =
    Radians(0)

  def fromDegrees(degrees: Double): Radians =
    Radians((TAU / 360d) * (degrees % 360d))

  def fromSeconds(seconds: Seconds): Radians =
    Radians(TAU * (seconds.toDouble % 1.0d))

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

    def wrap: Radians =
      Radians(((r % Radians.TAU) + Radians.TAU) % Radians.TAU)

    def negative: Radians =
      Radians(-r)

    def hash: String =
      r.toString()

    def toDouble: Double =
      r

    def toFloat: Float =
      r.toFloat
