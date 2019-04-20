package indigo.shared.audio

class Volume(val amount: Double) extends AnyVal {
  def *(other: Volume): Volume =
    Volume.product(this, other)
}
object Volume {
  val Min: Volume = Volume(0)
  val Max: Volume = Volume(1)

  def apply(volume: Double): Volume =
    new Volume(if (volume < 0) 0 else if (volume > 1) 1 else volume)

  def product(a: Volume, b: Volume): Volume =
    Volume(a.amount * b.amount)
}