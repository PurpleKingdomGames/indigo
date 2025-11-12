package indigoextras.performers

import indigo.SubSystemContext
import indigo.physics.Collider
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode

/** A pool of performers that can be updated and rendered.
  *
  * Very similar to the ActorPool, but cut down and specialised for performers.
  */
final case class PerformerPool[ReferenceData](
    performers: Batch[Performer[ReferenceData]],
    findColliderById: PerformerId => Option[Collider[PerformerId]]
):

  private lazy val _currentIds: Batch[PerformerId] =
    performers.map(_.id)

  /** Update the performer pool, passing in the model and a standard context. */
  def update(
      context: SubSystemContext[?],
      model: ReferenceData
  ): GlobalEvent => Outcome[PerformerPool[ReferenceData]] =
    val nextPool: GlobalEvent => Outcome[Batch[Performer[ReferenceData]]] =
      case FrameTick =>
        performers
          .map { p =>
            updatePerformer(PerformerContext(findById, findColliderById, model, context), p)(FrameTick)
          }
          .sequence
          .map { performerInstances =>
            performerInstances.sortBy(_.depth.value)
          }

      case e =>
        performers.map { p =>
          updatePerformer(PerformerContext(findById, findColliderById, model, context), p)(e)
        }.sequence

    (e: GlobalEvent) => nextPool(e).map(n => this.copy(performers = n))

  private def updatePerformer(
      context: PerformerContext[ReferenceData],
      performer: Performer[ReferenceData]
  ): GlobalEvent => Outcome[Performer[ReferenceData]] =
    case FrameTick =>
      performer match
        case p: Performer.Extra[ReferenceData] =>
          Outcome(p.update(context))

        case p: Performer.Stunt[ReferenceData] =>
          Outcome(p.update(context))

        case p: Performer.Support[ReferenceData] =>
          p.update(context)(FrameTick)

        case p: Performer.Lead[ReferenceData] =>
          p.update(context)(FrameTick)

    case e =>
      performer match
        case p: Performer.Extra[ReferenceData] =>
          Outcome(p)

        case p: Performer.Stunt[ReferenceData] =>
          Outcome(p)

        case p: Performer.Support[ReferenceData] =>
          p.update(context)(e)

        case p: Performer.Lead[ReferenceData] =>
          p.update(context)(e)

  def present(
      context: SubSystemContext[?],
      colliderLookup: PerformerId => Option[Collider[PerformerId]],
      model: ReferenceData
  ): Outcome[Batch[SceneNode]] =
    performers
      .map { p =>
        presentPerformer(PerformerContext(findById, findColliderById, model, context), colliderLookup, p)
      }
      .sequence
      .map(_.flatten)

  private def presentPerformer(
      context: PerformerContext[ReferenceData],
      colliderLookup: PerformerId => Option[Collider[PerformerId]],
      performer: Performer[ReferenceData]
  ): Outcome[Batch[SceneNode]] =
    performer match
      case p: Performer.Extra[ReferenceData] =>
        Outcome(p.present(context))

      case p: Performer.Stunt[ReferenceData] =>
        colliderLookup(p.id) match
          case None =>
            Outcome(Batch.empty)

          case Some(c) =>
            Outcome(p.present(context, c))

      case p: Performer.Support[ReferenceData] =>
        p.present(context)

      case p: Performer.Lead[ReferenceData] =>
        colliderLookup(p.id) match
          case None =>
            Outcome(Batch.empty)

          case Some(c) =>
            p.present(context, c)

  def findById(id: PerformerId): Option[Performer[ReferenceData]] =
    performers.find(_.id == id)

  /** Finds the first performer in the pool that matches the predicate test. */
  def find(p: Performer[ReferenceData] => Boolean): Option[Performer[ReferenceData]] =
    performers.find(perf => p(perf))

  /** Finds all performers in the pool that match the predicate test. */
  def filter(p: Performer[ReferenceData] => Boolean): Batch[Performer[ReferenceData]] =
    performers.filter(perf => p(perf))

  /** Finds all performers in the pool that do not match the predicate test. */
  def filterNot(p: Performer[ReferenceData] => Boolean): Batch[Performer[ReferenceData]] =
    performers.filterNot(perf => p(perf))

  /** Spawns a batch of new performers into the pool. */
  def spawn(
      newPerformers: Batch[Performer[ReferenceData]]
  ): PerformerPool[ReferenceData] =
    this.copy(
      performers = performers ++ newPerformers.filterNot(p => _currentIds.exists(_ == p.id))
    )

  /** Spawns new performers in the pool. */
  def spawn(newPerformers: Performer[ReferenceData]*): PerformerPool[ReferenceData] =
    spawn(Batch.fromSeq(newPerformers))

  /** Kills any performers in the pool that match the predicate test. */
  def kill(p: Performer[ReferenceData] => Boolean): PerformerPool[ReferenceData] =
    this.copy(
      performers = performers.filterNot(perf => p(perf))
    )

  def toBatch: Batch[Performer[ReferenceData]] =
    performers

object PerformerPool:

  def apply[ReferenceData](
      findColliderById: PerformerId => Option[Collider[PerformerId]]
  ): PerformerPool[ReferenceData] =
    PerformerPool(Batch.empty, findColliderById)
