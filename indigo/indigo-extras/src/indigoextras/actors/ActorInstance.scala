package indigoextras.actors

final case class ActorInstance[ReferenceData, ActorType](instance: ActorType, actor: Actor[ReferenceData, ActorType])
