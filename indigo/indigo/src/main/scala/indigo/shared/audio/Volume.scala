package indigo.shared.audio

import annotation.targetName

/** Audio volume amount.
  */
opaque type Volume = Double
object Volume:
  inline def apply(volume: Double): Volume =
    if (volume < 0) 0 else if (volume > 1) 1 else volume

  val Min: Volume = Volume(0)
  val Max: Volume = Volume(1)

  extension (v: Volume)
    def +(other: Volume): Volume = Volume(v + other)
    @targetName("+_Double")
    def +(other: Double): Volume = Volume(v + other)

    def -(other: Volume): Volume = Volume(v - other)
    @targetName("-_Double")
    def -(other: Double): Volume = Volume(v - other)

    def *(other: Volume): Volume = Volume(v * other)
    @targetName("*_Double")
    def *(other: Double): Volume = Volume(v * other)

    def /(other: Volume): Volume = Volume(v / other)
    @targetName("/_Double")
    def /(other: Double): Volume = Volume(v / other)

    def ~==(other: Volume): Boolean =
      Math.abs(v.toDouble - other.toDouble) < 0.001

    def toDouble: Double = v
