package indigo.shared.shader

opaque type ShaderId = String
object ShaderId:
  def apply(value: String): ShaderId = value
  given CanEqual[ShaderId, ShaderId] = CanEqual.derived
  given CanEqual[Option[ShaderId], Option[ShaderId]] = CanEqual.derived
