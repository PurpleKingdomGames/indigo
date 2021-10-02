package indigo.shared.time

import annotation.targetName

/** Represents a unit of time in seconds
 */
opaque type Seconds = Double
object Seconds:

  given CanEqual[Seconds, Seconds] = CanEqual.derived
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

    inline def toInt: Int =
      s.toInt

    inline def toLong: Long =
      s.toLong

    inline def toFloat: Float =
      s.toFloat

    inline def toDouble: Double =
      s

    inline def toMillis: Millis =
      Millis(Math.floor(s * 1000).toLong)

