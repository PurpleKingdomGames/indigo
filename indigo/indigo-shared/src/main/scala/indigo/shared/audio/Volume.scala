package indigo.shared.audio

final case class Volume(amount: Double) extends AnyVal {
  def *(other: Volume): Volume =
    Volume(this.amount * other.amount)

  override def toString(): String =
    s"Volume(${amount.toString})"
}
object Volume {
  val Min: Volume = Volume(0)
  val Max: Volume = Volume(1)

  def apply(volume: Double): Volume =
    new Volume(if (volume < 0) 0 else if (volume > 1) 1 else volume)
}
