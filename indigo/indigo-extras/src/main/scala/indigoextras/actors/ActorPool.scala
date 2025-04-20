package indigoextras.actors

import indigo.SubSystemContext
import indigo.scenes.SceneContext
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode

final case class ActorPool[ParentModel, A](
    actors: Batch[ActorInstance[ParentModel, A]]
)(using Ordering[A]):

  private val orderingInstance: Ordering[ActorInstance[ParentModel, A]] =
    Ordering.by(a => a.instance)

  /** Update the actor system, passing in the model and a standard context. */
  def update(
      context: Context[?],
      model: ParentModel
  ): GlobalEvent => Outcome[ActorPool[ParentModel, A]] =
    val nextPool: GlobalEvent => Outcome[Batch[ActorInstance[ParentModel, A]]] =
      case FrameTick =>
        actors
          .map { ai =>
            val ctx = ActorContext(find, model, context)

            ai.actor.updateModel(ctx, ai.instance)(FrameTick).map { updated =>
              ai.copy(instance = updated)
            }
          }
          .sequence
          .map { actorInstances =>
            actorInstances.sorted[ActorInstance[ParentModel, A]](orderingInstance)
          }

      case e =>
        actors
          .map { ai =>
            val ctx = ActorContext(find, model, context)

            ai.actor.updateModel(ctx, ai.instance)(e).map { updated =>
              ai.copy(instance = updated)
            }
          }
          .sequence

    (e: GlobalEvent) => nextPool(e).map(n => this.copy(actors = n))

  /** Update the actor system, passing in the model and a scene context. */
  def update(
      context: SceneContext[?],
      model: ParentModel
  ): GlobalEvent => Outcome[ActorPool[ParentModel, A]] =
    update(context.toContext, model)

  /** Update the actor system, passing in the model and a subsystem context. */
  def update(
      context: SubSystemContext[?],
      model: ParentModel
  ): GlobalEvent => Outcome[ActorPool[ParentModel, A]] =
    update(context.toContext, model)

  def present(
      context: Context[?],
      model: ParentModel
  ): Outcome[Batch[SceneNode]] =
    actors
      .map { ai =>
        val ctx = ActorContext(find, model, context)

        ai.actor.present(ctx, ai.instance)
      }
      .sequence
      .map(_.flatten)

  /** Finds the first actor in the system that matches the predicate test. */
  def find(p: A => Boolean): Option[A] =
    actors.find(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the system that match the predicate test. */
  def filter(p: A => Boolean): Batch[A] =
    actors.filter(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the system that do not match the predicate test. */
  def filterNot(p: A => Boolean): Batch[A] =
    actors.filterNot(ai => p(ai.instance)).map(_.instance)

  /** Spawns a batch of new actor in the system. */
  def spawn[B <: A](newActors: Batch[B])(using actor: Actor[ParentModel, B]): ActorPool[ParentModel, A] =
    this.copy(
      actors = actors ++ newActors.map(a => ActorInstance(a, actor.asInstanceOf[Actor[ParentModel, A]]))
    )

  /** Spawns new actors in the system. */
  def spawn[B <: A](newActors: B*)(using actor: Actor[ParentModel, B]): ActorPool[ParentModel, A] =
    spawn(Batch.fromSeq(newActors))

  /** Kills any actors in the system that match the predicate test. */
  def kill(p: A => Boolean): ActorPool[ParentModel, A] =
    this.copy(
      actors = actors.filterNot(ai => p(ai.instance))
    )

object ActorPool:

  def empty[ParentModel, A](using Ordering[A]): ActorPool[ParentModel, A] =
    apply()

  def apply[ParentModel, A]()(using Ordering[A]): ActorPool[ParentModel, A] =
    ActorPool(Batch.empty)
