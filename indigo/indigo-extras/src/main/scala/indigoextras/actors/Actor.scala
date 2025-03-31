package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[ParentRefData, A]:

  /** The actor's reference data is a snapshot of the game's model that is specific to this actor.
    */
  type ReferenceData

  def reference(parentReferenceData: ParentRefData): ReferenceData

  /** The depth of this actor in the scene.
    */
  def depth(context: ActorContext[ReferenceData], actor: A): Int

  /** Update this actor's model.
    */
  def updateModel(context: ActorContext[ReferenceData], actor: A): GlobalEvent => Outcome[A]

  /** Draw the actor, based on the actor's model.
    */
  def present(context: ActorContext[ReferenceData], actor: A): Outcome[Batch[SceneNode]]
