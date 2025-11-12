package indigoextras.performers

opaque type PerformerDepth = Int

object PerformerDepth:
  def apply(value: Int): PerformerDepth = value

  val zero: PerformerDepth = 0

  extension (d: PerformerDepth)
    def value: Int = d
    def toInt: Int = d

  given CanEqual[PerformerDepth, PerformerDepth] = CanEqual.derived
