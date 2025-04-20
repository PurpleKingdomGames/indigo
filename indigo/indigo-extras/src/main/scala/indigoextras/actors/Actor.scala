package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[ParentModel, A]:

  /** Update this actor's model.
    */
  def updateModel(context: ActorContext[ParentModel, A], actor: A): GlobalEvent => Outcome[A]

  /** Draw the actor, based on the actor's model.
    */
  def present(context: ActorContext[ParentModel, A], actor: A): Outcome[Batch[SceneNode]]

