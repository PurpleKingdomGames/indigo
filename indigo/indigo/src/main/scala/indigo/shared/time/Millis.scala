package indigo.shared.time

import annotation.targetName

/** Represents a unit of time in milliseconds
 */
opaque type Millis = Long
object Millis:
  inline def apply(millis: Long): Millis = millis

  given CanEqual[Millis, Millis] = CanEqual.derived
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

    inline def toInt: Int =
      ms.toInt

    inline def toLong: Long =
      ms

    inline def toFloat: Float =
      ms.toFloat

    inline def toDouble: Double =
      ms.toDouble

    inline def toSeconds: Seconds =
      Seconds(ms.toDouble / 1000)
