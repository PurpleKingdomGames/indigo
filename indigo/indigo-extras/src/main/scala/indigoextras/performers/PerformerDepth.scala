package indigoextras.performers

opaque type PerformerDepth = Int

object PerformerDepth:
  def apply(value: Int): PerformerDepth = value

  extension (d: PerformerDepth)
    def value: Int = d
    def toInt: Int = d

  given CanEqual[PerformerDepth, PerformerDepth] = CanEqual.derived
