package indigoextras.actors

final case class ActorInstance[ReferenceData, A](instance: A, actor: Actor[ReferenceData, A])
