package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[ReferenceData, ActorType]:

  /** Update this actor's model.
    */
  def updateModel(context: ActorContext[ReferenceData, ActorType], actor: ActorType): GlobalEvent => Outcome[ActorType]

  /** Draw the actor, based on the actor's model.
    */
  def present(context: ActorContext[ReferenceData, ActorType], actor: ActorType): Outcome[Batch[SceneNode]]
