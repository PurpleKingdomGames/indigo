package indigoextras.performers

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneNode

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
