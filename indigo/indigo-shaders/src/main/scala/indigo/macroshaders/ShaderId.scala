package indigo.macroshaders

opaque type ShaderId = String
object ShaderId:
  def apply(id: String): ShaderId = id
