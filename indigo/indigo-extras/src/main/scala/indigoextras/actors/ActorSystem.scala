package indigoextras.actors

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId

/*

What am I doing?

1. Restoring a sub system version of Actors, initially just with one ActorPool
  1. spawn / kill via events
  2. find via context (supplied from ActorSystem, not ActorPool)
  3. Dumber depth management
2. Tree of pools
  1. Events to reorder / reparent
  2. Trees render to layers
3. Types of actors
  1. Full
  2. Lite
4. Physics out-of-the-box
  1. Might be another actor type?

 */

final case class ActorSystem[GameModel, RefData, ActorType](
    id: SubSystemId,
    layerKey: Option[LayerKey],
    extractReference: GameModel => RefData,
    _initialActors: Batch[ActorInstance[RefData, ActorType]],
    _updateActorPool: ActorPool[RefData, ActorType] => PartialFunction[GlobalEvent, Outcome[
      ActorPool[RefData, ActorType]
    ]]
)(using Ordering[ActorType])
    extends SubSystem[GameModel]:

  type EventType      = GlobalEvent
  type SubSystemModel = ActorPool[RefData, ActorType]
  type ReferenceData  = RefData

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: GameModel): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[SubSystemModel] =
    Outcome(ActorPool(_initialActors))

  def update(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[ReferenceData, ActorType]
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, ActorType]] =
    _updateActorPool(model)
      .orElse { case e =>
        model.update(context, context.reference)(e)
      }

  def present(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[ReferenceData, ActorType]
  ): Outcome[SceneUpdateFragment] =
    model.present(context, context.reference).map { ns =>
      layerKey match
        case None =>
          SceneUpdateFragment(
            Layer.Content(ns)
          )

        case Some(key) =>
          SceneUpdateFragment(
            key -> Layer.Content(ns)
          )
    }

  def withId(id: SubSystemId): ActorSystem[GameModel, ReferenceData, ActorType] =
    this.copy(id = id)

  def withLayerKey(key: LayerKey): ActorSystem[GameModel, ReferenceData, ActorType] =
    this.copy(layerKey = Some(key))
  def clearLayerKey: ActorSystem[GameModel, ReferenceData, ActorType] =
    this.copy(layerKey = None)

  def spawn[B <: ActorType](actor: B)(using
      a: Actor[ReferenceData, B]
  ): ActorSystem[GameModel, ReferenceData, ActorType] =
    this.copy(_initialActors = _initialActors :+ ActorInstance(actor, a.asInstanceOf[Actor[ReferenceData, ActorType]]))

  def updateActors(
      f: ActorPool[ReferenceData, ActorType] => PartialFunction[GlobalEvent, Outcome[
        ActorPool[ReferenceData, ActorType]
      ]]
  ): ActorSystem[GameModel, ReferenceData, ActorType] =
    this.copy(_updateActorPool = f)

object ActorSystem:

  private def noUpdate[ReferenceData, ActorType]: ActorPool[ReferenceData, ActorType] => PartialFunction[
    GlobalEvent,
    Outcome[ActorPool[ReferenceData, ActorType]]
  ] =
    _ => PartialFunction.empty

  def apply[GameModel, ActorType](
      id: SubSystemId
  )(using Ordering[ActorType]): ActorSystem[GameModel, GameModel, ActorType] =
    ActorSystem(id, None, identity, Batch.empty, noUpdate[GameModel, ActorType])

  def apply[GameModel, ActorType](
      id: SubSystemId,
      layerKey: LayerKey
  )(using Ordering[ActorType]): ActorSystem[GameModel, GameModel, ActorType] =
    ActorSystem(id, Some(layerKey), identity, Batch.empty, noUpdate[GameModel, ActorType])

  def apply[GameModel, ReferenceData, ActorType](
      id: SubSystemId,
      layerKey: LayerKey,
      extractReference: GameModel => ReferenceData
  )(using Ordering[ActorType]): ActorSystem[GameModel, ReferenceData, ActorType] =
    ActorSystem(id, Some(layerKey), extractReference, Batch.empty, noUpdate[ReferenceData, ActorType])
