package indigoextras.actors

import indigo.shared.events.GlobalEvent

enum ActorEvent extends GlobalEvent:
  case Spawn(actor: Actor[?])
  case Kill(id: ActorId)
