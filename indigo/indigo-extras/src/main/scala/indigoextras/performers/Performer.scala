package indigoextras.performers

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode
import indigoextras.actors.Actor
import indigoextras.actors.ActorContext

// TODO: Might need to remove the ReferenceData type, generally unprovable. Use an 'extract your own' approach instead?
trait Performer[ReferenceData]:

  /** A unique identifier for the performer.
    */
  def id: PerformerId

  // TODO: Depth? Location? Position? Either the position is managed in the Stage manager and altered by events, or the performer says where it ought to be.
  /** Provide the depth / order of the performer
    */
  def depth: PerformerDepth

  // TODO: Should the performer say which layer it's targeting? Or something?

  /** Update the performer
    */
  def update(context: PerformerContext[ReferenceData]): GlobalEvent => Outcome[Performer[ReferenceData]]

  /** Draw the performer
    */
  def present(context: PerformerContext[ReferenceData]): Outcome[Batch[SceneNode]]

object Performer:

  given [ReferenceData] => Ordering[Performer[ReferenceData]] =
    Ordering.by(_.depth.value)

  def makeActor[ReferenceData](
      find: (Performer[ReferenceData] => Boolean) => Option[Performer[ReferenceData]]
  ): Actor[ReferenceData, Performer[ReferenceData]] =
    new Actor[ReferenceData, Performer[ReferenceData]]:

      def update(
          context: ActorContext[ReferenceData, Performer[ReferenceData]],
          performer: Performer[ReferenceData]
      ): GlobalEvent => Outcome[Performer[ReferenceData]] =
        e => performer.update(PerformerContext.fromActorContext(context, find))(e)

      def present(
          context: ActorContext[ReferenceData, Performer[ReferenceData]],
          performer: Performer[ReferenceData]
      ): Outcome[Batch[SceneNode]] =
        performer.present(PerformerContext.fromActorContext(context, find))
