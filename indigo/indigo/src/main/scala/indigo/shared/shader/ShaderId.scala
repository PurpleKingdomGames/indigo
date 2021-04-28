package indigo.shared.shader

opaque type ShaderId = String
object ShaderId:
  def apply(value: String): ShaderId = value
