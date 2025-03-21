package indigoextras.actors

import indigo.GlobalEvent
import indigo.Outcome
import indigo.shared.Context
import indigo.shared.collections.Batch
import indigo.shared.scenegraph.SceneNode

trait Actor[Model]:

  type ActorModel

  /** The unique identifier for this actor.
    */
  def id: ActorId

  def read(model: Model): ActorModel

  /** The depth of this actor in the scene.
    */
  def depth(context: Context[Unit], model: ActorModel): Int =
    0

  /** Update this actor's model.
    */
  def updateModel(
      context: Context[Unit],
      model: ActorModel
  ): GlobalEvent => Outcome[Actor[Model]] =
    _ => Outcome(this)

  /** Produce a renderable output for this actor, based on the actor's model.
    */
  def present(
      context: Context[Unit],
      model: ActorModel
  ): Outcome[Batch[SceneNode]] =
    Outcome(Batch.empty)
