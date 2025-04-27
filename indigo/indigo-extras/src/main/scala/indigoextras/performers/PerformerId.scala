package indigoextras.performers

/** The unique identifier for a performer.
  *
  * Technically speaking, there is nothing preventing the use of the same id for multiple performers, but it will cause
  * unexpected behaviour.
  */
opaque type PerformerId = String

object PerformerId:
  def apply(value: String): PerformerId = value

  extension (a: PerformerId)
    def value: String    = a
    def toString: String = a

  given CanEqual[PerformerId, PerformerId] = CanEqual.derived
