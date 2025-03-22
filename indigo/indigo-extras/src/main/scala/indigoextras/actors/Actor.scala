package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[Model]:

  /** The actor's reference data is a snapshot of the game's model that is specific to this actor.
    */
  type ReferenceData

  /** The unique identifier for this actor.
    */
  def id: ActorId

  /** Read the game's model and produce the actor's reference model.
    */
  def reference(model: Model): ReferenceData

  /** The depth of this actor in the scene.
    */
  def depth(context: ActorContext, reference: ReferenceData): Int

  /** Update this actor's model.
    */
  def updateModel(
      context: ActorContext,
      reference: ReferenceData
  ): GlobalEvent => Outcome[Actor[Model]]

  /** Draw the actor, based on the actor's model.
    */
  def present(
      context: ActorContext,
      reference: ReferenceData
  ): Outcome[Batch[SceneNode]]
