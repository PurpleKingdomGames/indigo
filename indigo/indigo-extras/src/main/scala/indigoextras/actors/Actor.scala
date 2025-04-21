package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[ReferenceData, ActorType]:

  /** Update this actor.
    */
  def update(context: ActorContext[ReferenceData, ActorType], actor: ActorType): GlobalEvent => Outcome[ActorType]

  /** Draw the actor.
    */
  def present(context: ActorContext[ReferenceData, ActorType], actor: ActorType): Outcome[Batch[SceneNode]]
