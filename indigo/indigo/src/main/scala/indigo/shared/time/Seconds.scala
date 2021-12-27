package indigo.shared.time

import annotation.targetName

/** Represents a unit of time in seconds
  */
opaque type Seconds = Double
object Seconds:

  given CanEqual[Seconds, Seconds]                 = CanEqual.derived
  given CanEqual[Option[Seconds], Option[Seconds]] = CanEqual.derived

  inline def apply(seconds: Double): Seconds = seconds

  val zero: Seconds =
    Seconds(0)

  extension (s: Seconds)
    def +(other: Seconds): Seconds =
      Seconds(s + other)
    @targetName("+_Double")
    def +(other: Double): Seconds =
      Seconds(s + other)

    def -(other: Seconds): Seconds =
      Seconds(s - other)
    @targetName("-_Double")
    def -(other: Double): Seconds =
      Seconds(s - other)

    def *(other: Seconds): Seconds =
      Seconds(s * other)
    @targetName("*_Double")
    def *(other: Double): Seconds =
      Seconds(s * other)

    def /(other: Seconds): Seconds =
      Seconds(s / other)
    @targetName("/_Double")
    def /(other: Double): Seconds =
      Seconds(s / other)

    def %(other: Seconds): Seconds =
      Seconds(s % other)
    @targetName("%_Double")
    def %(other: Double): Seconds =
      Seconds(s % other)

    def <(other: Seconds): Boolean =
      s < other

    def >(other: Seconds): Boolean =
      s > other

    def <=(other: Seconds): Boolean =
      s <= other

    def >=(other: Seconds): Boolean =
      s >= other

    def abs: Seconds =
      Seconds(Math.abs(s.toDouble))

    def min(other: Seconds): Seconds =
      Seconds(Math.min(s.toDouble, other.toDouble))

    def max(other: Seconds): Seconds =
      Seconds(Math.max(s.toDouble, other.toDouble))

    def clamp(min: Seconds, max: Seconds): Seconds =
      Seconds(Math.min(max.toDouble, Math.max(min.toDouble, s.toDouble)))

    def ~==(other: Seconds): Boolean =
      Math.abs(s.toDouble - other.toDouble) < 0.001

    def toInt: Int =
      s.toInt

    def toLong: Long =
      s.toLong

    def toFloat: Float =
      s.toFloat

    def toDouble: Double =
      s

    def toMillis: Millis =
      Millis(Math.floor(s * 1000).toLong)
