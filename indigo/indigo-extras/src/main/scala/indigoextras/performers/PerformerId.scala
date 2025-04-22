package indigoextras.performers

opaque type PerformerId = String

object PerformerId:
  def apply(value: String): PerformerId = value

  extension (a: PerformerId)
    def value: String    = a
    def toString: String = a

  given CanEqual[PerformerId, PerformerId] = CanEqual.derived
