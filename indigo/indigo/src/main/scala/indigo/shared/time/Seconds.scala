package indigo.shared.time

opaque type Seconds = Double
object Seconds:

  def apply(seconds: Double): Seconds = seconds

  val zero: Seconds =
    Seconds(0)

  extension (s: Seconds)
    def +(other: Seconds): Seconds =
      Seconds(s + other)

    def -(other: Seconds): Seconds =
      Seconds(s - other)

    def *(other: Seconds): Seconds =
      Seconds(s * other)

    def /(other: Seconds): Seconds =
      Seconds(s / other)

    def %(other: Seconds): Seconds =
      Seconds(s % other)

    def <(other: Seconds): Boolean =
      s < other

    def >(other: Seconds): Boolean =
      s > other

    def <=(other: Seconds): Boolean =
      s <= other

    def >=(other: Seconds): Boolean =
      s >= other

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

