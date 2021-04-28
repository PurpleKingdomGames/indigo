package indigo.shared.audio

opaque type Volume = Double
object Volume:
  def apply(volume: Double): Volume =
    if (volume < 0) 0 else if (volume > 1) 1 else volume

  val Min: Volume = Volume(0)
  val Max: Volume = Volume(1)

  extension (v: Volume)
    def *(other: Volume): Volume = Volume(v * other)
    def toDouble: Double         = v
