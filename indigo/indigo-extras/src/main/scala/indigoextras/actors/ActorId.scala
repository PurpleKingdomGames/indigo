package indigoextras.actors

opaque type ActorId = String

object ActorId:
  def apply(value: String): ActorId = value

  extension (a: ActorId)
    def value: String    = a
    def toString: String = a

  given CanEqual[ActorId, ActorId] = CanEqual.derived
