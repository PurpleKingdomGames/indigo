package indigoextras.actors

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId

final case class ActorSystem[Model](
    id: SubSystemId,
    layerKey: Option[String],
    initialActors: Batch[Actor[Model]]
) extends SubSystem[Model]:

  type EventType      = GlobalEvent
  type SubSystemModel = ActorSystemModel[Model]
  type ReferenceData  = Model

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: Model): Model =
    model

  def initialModel: Outcome[ActorSystemModel[Model]] =
    Outcome(ActorSystemModel[Model](initialActors))

  def update(
      context: SubSystemContext[Model],
      model: ActorSystemModel[Model]
  ): GlobalEvent => Outcome[ActorSystemModel[Model]] =
    case ActorEvent.Spawn(actor) =>
      Outcome(
        ActorSystemModel(
          model.actors :+ actor.asInstanceOf[Actor[Model]]
        )
      )

    case ActorEvent.Kill(id) =>
      Outcome(
        ActorSystemModel(
          model.actors.filterNot(ai => ai.id == id)
        )
      )

    case e =>
      val ctx =
        context.toContext

      model.actors
        .sortBy(ai => ai.depth(ctx, ai.read(context.reference)))
        .map { ai =>
          ai.updateModel(ctx, ai.read(context.reference))(e)
        }
        .sequence
        .map(ActorSystemModel.apply)

  def present(
      context: SubSystemContext[Model],
      model: ActorSystemModel[Model]
  ): Outcome[SceneUpdateFragment] =
    val ctx =
      context.toContext

    val nodes: Outcome[Batch[SceneNode]] =
      model.actors
        .map { ai =>
          ai.present(ctx, ai.read(context.reference))
        }
        .sequence
        .map(_.flatten)

    layerKey match
      case None =>
        nodes.map { ns =>
          SceneUpdateFragment(
            Layer.Content(ns)
          )
        }

      case Some(key) =>
        nodes.map { ns =>
          SceneUpdateFragment(
            LayerKey(key) -> Layer.Content(ns)
          )
        }

  def spawn[A](actors: Batch[Actor[ReferenceData]]): ActorSystem[Model] =
    this.copy(initialActors = initialActors ++ actors)
  def spawn(actor: Actor[ReferenceData]*): ActorSystem[Model] =
    spawn(Batch.fromSeq(actor))

object ActorSystem:

  def apply[SandboxGameModel](
      id: SubSystemId
  ): ActorSystem[SandboxGameModel] =
    ActorSystem(id, None, Batch.empty)

final case class ActorSystemModel[Model](actors: Batch[Actor[Model]])
