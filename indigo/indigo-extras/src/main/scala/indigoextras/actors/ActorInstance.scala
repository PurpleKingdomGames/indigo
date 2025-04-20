package indigoextras.actors

final case class ActorInstance[ParentModel, A](instance: A, actor: Actor[ParentModel, A])