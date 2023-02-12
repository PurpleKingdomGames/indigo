package indigo.shared.time

import annotation.targetName

/** Represents a unit of time in milliseconds
  */
opaque type Millis = Long
object Millis:
  inline def apply(millis: Long): Millis = millis

  given CanEqual[Millis, Millis]                 = CanEqual.derived
  given CanEqual[Option[Millis], Option[Millis]] = CanEqual.derived

  val zero: Millis =
    Millis(0)

  extension (ms: Millis)

    def +(other: Millis): Millis =
      Millis(ms + other)
    @targetName("+_Long")
    def +(other: Long): Millis =
      Millis(ms + other)

    def -(other: Millis): Millis =
      Millis(ms - other)
    @targetName("-_Long")
    def -(other: Long): Millis =
      Millis(ms - other)

    def *(other: Millis): Millis =
      Millis(ms * other)
    @targetName("*_Long")
    def *(other: Long): Millis =
      Millis(ms * other)

    def /(other: Millis): Millis =
      Millis(ms / other)
    @targetName("/_Long")
    def /(other: Long): Millis =
      Millis(ms / other)

    def %(other: Millis): Millis =
      Millis(ms % other)
    @targetName("%_Long")
    def %(other: Long): Millis =
      Millis(ms % other)

    def <(other: Millis): Boolean =
      ms < other

    def >(other: Millis): Boolean =
      ms > other

    def <=(other: Millis): Boolean =
      ms <= other

    def >=(other: Millis): Boolean =
      ms >= other

    def abs: Millis =
      Millis(Math.abs(ms.toLong))

    def min(other: Millis): Millis =
      Millis(Math.min(ms.toLong, other.toLong))

    def max(other: Millis): Millis =
      Millis(Math.max(ms.toLong, other.toLong))

    def clamp(min: Millis, max: Millis): Millis =
      Millis(Math.min(max.toLong, Math.max(min.toLong, ms.toLong)))

    def toInt: Int =
      ms.toInt

    def toLong: Long =
      ms

    def toFloat: Float =
      ms.toFloat

    def toDouble: Double =
      ms.toDouble

    def toSeconds: Seconds =
      Seconds(ms.toDouble / 1000)
