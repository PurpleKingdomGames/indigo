package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[ReferenceData, A]:

  /** Update this actor's model.
    */
  def updateModel(context: ActorContext[ReferenceData, A], actor: A): GlobalEvent => Outcome[A]

  /** Draw the actor, based on the actor's model.
    */
  def present(context: ActorContext[ReferenceData, A], actor: A): Outcome[Batch[SceneNode]]
