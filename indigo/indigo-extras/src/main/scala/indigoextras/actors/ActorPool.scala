package indigoextras.actors

import indigo.shared.collections.Batch

final case class ActorPool[ReferenceData, A](pool: Batch[ActorInstance[ReferenceData, A]]):

  def spawn[B <: A](newActors: Batch[B])(using actor: Actor[ReferenceData, B]): ActorPool[ReferenceData, A] =
    ActorPool(
      pool ++ newActors.map(a => ActorInstance(a, actor.asInstanceOf[Actor[ReferenceData, A]]))
    )
  def spawn[B <: A](newActors: B*)(using actor: Actor[ReferenceData, B]): ActorPool[ReferenceData, A] =
    spawn(Batch.fromSeq(newActors))

  def kill(p: A => Boolean): ActorPool[ReferenceData, A] =
    ActorPool(
      pool.filterNot(ai => p(ai.instance))
    )
