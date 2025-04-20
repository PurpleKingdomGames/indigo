// package indigoextras.actors

// import indigo.shared.collections.Batch

// final case class ActorPool[ParentModel, A](pool: Batch[ActorInstance[ParentModel, A]]):

//   def find(p: A => Boolean): Option[A] =
//     pool.find(ai => p(ai.instance)).map(_.instance)

//   def spawn[B <: A](newActors: Batch[B])(using actor: Actor[ParentModel, B]): ActorPool[ParentModel, A] =
//     ActorPool(
//       pool ++ newActors.map(a => ActorInstance(a, actor.asInstanceOf[Actor[ParentModel, A]]))
//     )
//   def spawn[B <: A](newActors: B*)(using actor: Actor[ParentModel, B]): ActorPool[ParentModel, A] =
//     spawn(Batch.fromSeq(newActors))

//   def kill(p: A => Boolean): ActorPool[ParentModel, A] =
//     ActorPool(
//       pool.filterNot(ai => p(ai.instance))
//     )

// object ActorPool:
//   def empty[ParentModel, A]: ActorPool[ParentModel, A] =
//     ActorPool(Batch.empty)