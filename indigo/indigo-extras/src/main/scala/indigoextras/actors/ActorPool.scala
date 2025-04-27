package indigoextras.actors

import indigo.SubSystemContext
import indigo.scenes.SceneContext
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode

final case class ActorPool[ReferenceData, ActorType](
    actors: Batch[ActorInstance[ReferenceData, ActorType]]
)(using Ordering[ActorType]):

  private val orderingInstance: Ordering[ActorInstance[ReferenceData, ActorType]] =
    Ordering.by(a => a.instance)

  /** Update the actor pool, passing in the model and a standard context. */
  def update(
      context: Context[?],
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType]] =
    val nextPool: GlobalEvent => Outcome[Batch[ActorInstance[ReferenceData, ActorType]]] =
      case FrameTick =>
        actors
          .map { ai =>
            val ctx = ActorContext(find, model, context)

            ai.actor.update(ctx, ai.instance)(FrameTick).map { updated =>
              ai.copy(instance = updated)
            }
          }
          .sequence
          .map { actorInstances =>
            actorInstances.sorted[ActorInstance[ReferenceData, ActorType]](orderingInstance)
          }

      case e =>
        actors.map { ai =>
          val ctx = ActorContext(find, model, context)

          ai.actor.update(ctx, ai.instance)(e).map { updated =>
            ai.copy(instance = updated)
          }
        }.sequence

    (e: GlobalEvent) => nextPool(e).map(n => this.copy(actors = n))

  /** Update the actor pool, passing in the model and a scene context. */
  def update(
      context: SceneContext[?],
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType]] =
    update(context.toContext, model)

  /** Update the actor pool, passing in the model and a subsystem context. */
  def update(
      context: SubSystemContext[?],
      model: ReferenceData
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType]] =
    update(context.toContext, model)

  def present(
      context: Context[?],
      model: ReferenceData
  ): Outcome[Batch[SceneNode]] =
    actors
      .map { ai =>
        val ctx = ActorContext(find, model, context)

        ai.actor.present(ctx, ai.instance)
      }
      .sequence
      .map(_.flatten)

  def present(
      context: SceneContext[?],
      model: ReferenceData
  ): Outcome[Batch[SceneNode]] =
    present(context.toContext, model)

  def present(
      context: SubSystemContext[?],
      model: ReferenceData
  ): Outcome[Batch[SceneNode]] =
    present(context.toContext, model)

  /** Finds the first actor in the pool that matches the predicate test. */
  def find(p: ActorType => Boolean): Option[ActorType] =
    actors.find(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the pool that match the predicate test. */
  def filter(p: ActorType => Boolean): Batch[ActorType] =
    actors.filter(ai => p(ai.instance)).map(_.instance)

  /** Finds all actors in the pool that do not match the predicate test. */
  def filterNot(p: ActorType => Boolean): Batch[ActorType] =
    actors.filterNot(ai => p(ai.instance)).map(_.instance)

  /** Spawns a batch of new actor in the pool. */
  def spawn[B <: ActorType](
      newActors: Batch[B]
  )(using actor: Actor[ReferenceData, B]): ActorPool[ReferenceData, ActorType] =
    this.copy(
      actors = actors ++ newActors.map(a => ActorInstance(a, actor.asInstanceOf[Actor[ReferenceData, ActorType]]))
    )

  /** Spawns new actors in the pool. */
  def spawn[B <: ActorType](newActors: B*)(using actor: Actor[ReferenceData, B]): ActorPool[ReferenceData, ActorType] =
    spawn(Batch.fromSeq(newActors))

  /** Kills any actors in the pool that match the predicate test. */
  def kill(p: ActorType => Boolean): ActorPool[ReferenceData, ActorType] =
    this.copy(
      actors = actors.filterNot(ai => p(ai.instance))
    )

  def toBatch: Batch[ActorType] =
    actors.map(_.instance)

object ActorPool:

  def empty[ReferenceData, ActorType](using Ordering[ActorType]): ActorPool[ReferenceData, ActorType] =
    apply()

  def apply[ReferenceData, ActorType]()(using Ordering[ActorType]): ActorPool[ReferenceData, ActorType] =
    ActorPool(Batch.empty)
