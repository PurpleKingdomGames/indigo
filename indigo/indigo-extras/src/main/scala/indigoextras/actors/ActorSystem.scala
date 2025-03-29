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

final case class ActorSystem[Model, RefData](
    id: SubSystemId,
    layerKey: Option[LayerKey],
    initialActors: Batch[Actor[RefData]],
    extractReference: Model => RefData,
    updateActorPool: (ActorContext, ActorPool[RefData]) => PartialFunction[GlobalEvent, Outcome[ActorPool[RefData]]]
) extends SubSystem[Model]:

  type EventType      = GlobalEvent
  type SubSystemModel = ActorPool[RefData]
  type ReferenceData  = RefData

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: Model): RefData =
    extractReference(model)

  def initialModel: Outcome[ActorPool[RefData]] =
    Outcome(ActorPool[RefData](initialActors))

  def update(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[RefData]
  ): GlobalEvent => Outcome[ActorPool[RefData]] =
    val ctx = ActorContext(context)

    updateActorPool(ctx, model)
      .orElse { case e =>
        model.pool
          .sortBy(ai => ai.depth(ctx, ai.reference(context.reference)))
          .map { ai =>
            ai.updateModel(ctx, ai.reference(context.reference))(e)
          }
          .sequence
          .map(ActorPool.apply)
      }

  def present(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[RefData]
  ): Outcome[SceneUpdateFragment] =
    val ctx =
      ActorContext(context)

    val nodes: Outcome[Batch[SceneNode]] =
      model.pool
        .map { ai =>
          ai.present(ctx, ai.reference(context.reference))
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
            key -> Layer.Content(ns)
          )
        }

  def withId(id: SubSystemId): ActorSystem[Model, RefData] =
    this.copy(id = id)

  def withLayerKey(key: LayerKey): ActorSystem[Model, RefData] =
    this.copy(layerKey = Some(key))
  def clearLayerKey: ActorSystem[Model, RefData] =
    this.copy(layerKey = None)

  def spawn(actors: Batch[Actor[ReferenceData]]): ActorSystem[Model, RefData] =
    this.copy(initialActors = initialActors ++ actors)
  def spawn(actor: Actor[ReferenceData]*): ActorSystem[Model, RefData] =
    spawn(Batch.fromSeq(actor))

  def updateActors(
      f: (ActorContext, ActorPool[RefData]) => PartialFunction[GlobalEvent, Outcome[ActorPool[RefData]]]
  ): ActorSystem[Model, RefData] =
    this.copy(updateActorPool = f)

object ActorSystem:

  private def noUpdate[RD]: (ActorContext, ActorPool[RD]) => PartialFunction[GlobalEvent, Outcome[ActorPool[RD]]] =
    (_, _) => PartialFunction.empty

  def apply[Model](
      id: SubSystemId
  ): ActorSystem[Model, Model] =
    ActorSystem(id, None, Batch.empty, identity, noUpdate[Model])

  def apply[Model](
      id: SubSystemId,
      layerKey: LayerKey
  ): ActorSystem[Model, Model] =
    ActorSystem(id, Some(layerKey), Batch.empty, identity, noUpdate[Model])

  def apply[Model](
      id: SubSystemId,
      layerKey: LayerKey,
      initialActors: Batch[Actor[Model]]
  ): ActorSystem[Model, Model] =
    ActorSystem(id, Some(layerKey), Batch.empty, identity, noUpdate[Model])

  def apply[Model, ReferenceData](
      id: SubSystemId,
      layerKey: LayerKey,
      initialActors: Batch[Actor[Model]],
      extractReference: Model => ReferenceData
  ): ActorSystem[Model, ReferenceData] =
    ActorSystem(id, Some(layerKey), Batch.empty, extractReference, noUpdate[ReferenceData])

final case class ActorPool[ReferenceData](pool: Batch[Actor[ReferenceData]]):

  def spawn(newActors: Batch[Actor[ReferenceData]]): ActorPool[ReferenceData] =
    ActorPool(
      pool ++ newActors
    )
  def spawn(newActors: Actor[ReferenceData]*): ActorPool[ReferenceData] =
    spawn(Batch.fromSeq(newActors))

  def kill(ids: Batch[ActorId]): ActorPool[ReferenceData] =
    ActorPool(
      pool.filterNot(ai => ids.exists(_ == ai.id))
    )
  def kill(ids: ActorId*): ActorPool[ReferenceData] =
    kill(Batch.fromSeq(ids))
