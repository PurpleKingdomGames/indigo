package indigoextras.performers

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigoextras.actors.*

/*

What am I doing?

1. DONE ~~ Restoring a sub system version of Actors, initially just with one ActorPool
  1. DONE ~~ spawn / kill via events
  2. DONE? Check ~~ find via context (supplied from ActorSystem, not ActorPool)
  3. DONE? Check ~~ Dumber depth management
2. Tree of pools? Or just a list?
  1. Events to reorder / reparent
  2. Trees render to layers
3. Types of actors
  1. Full
  2. Lite
4. Physics out-of-the-box
  1. Might be another actor type?

 */

final case class StageManager[GameModel, RefData](
    id: SubSystemId,
    layerKey: Option[LayerKey],
    extractReference: GameModel => RefData,
    _initialPerformers: Batch[ActorInstance[RefData, Performer[RefData]]],
    _updateActorPool: ActorPool[RefData, Performer[RefData]] => PartialFunction[GlobalEvent, Outcome[
      ActorPool[RefData, Performer[RefData]]
    ]]
) extends SubSystem[GameModel]:

  type EventType      = GlobalEvent
  type SubSystemModel = ActorPool[RefData, Performer[RefData]]
  type ReferenceData  = RefData

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: GameModel): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[SubSystemModel] =
    Outcome(ActorPool(_initialPerformers))

  def update(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[ReferenceData, Performer[ReferenceData]]
  ): GlobalEvent => Outcome[ActorPool[ReferenceData, Performer[ReferenceData]]] =
    handlePerformerEvents(model).orElse {
      _updateActorPool(model)
        .orElse { case e =>
          model.update(context, context.reference)(e)
        }
    }

  private def handlePerformerEvents(
      model: ActorPool[ReferenceData, Performer[ReferenceData]]
  ): PartialFunction[GlobalEvent, Outcome[ActorPool[ReferenceData, Performer[ReferenceData]]]] = {
    case PerformerEvent.Spawn(performer) =>
      val p: Actor[ReferenceData, Performer[ReferenceData]] =
        Performer.makeActor(model.find)

      Outcome(
        model.spawn(Batch(performer.asInstanceOf[Performer[ReferenceData]]))(using p)
      )

    case PerformerEvent.Kill(id) =>
      Outcome(
        model.kill(_.id == id)
      )

    case PerformerEvent.KillAll(ids) =>
      Outcome(
        model.kill(p => ids.exists(_ == p.id))
      )
  }

  def present(
      context: SubSystemContext[ReferenceData],
      model: ActorPool[ReferenceData, Performer[ReferenceData]]
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

  def withId(id: SubSystemId): StageManager[GameModel, ReferenceData] =
    this.copy(id = id)

  def withLayerKey(key: LayerKey): StageManager[GameModel, ReferenceData] =
    this.copy(layerKey = Some(key))
  def clearLayerKey: StageManager[GameModel, ReferenceData] =
    this.copy(layerKey = None)

  // Can't do this here, need access to the pool to make the find function.
  // def spawn(performer: Performer[ReferenceData]): StageManager[GameModel, ReferenceData] =
  //   val p: Actor[ReferenceData, Performer[ReferenceData]] =
  //     Performer.makeActor()

  //   this.copy(_initialPerformers = _initialPerformers :+ ActorInstance(performer, p))

  def updateActors(
      f: ActorPool[ReferenceData, Performer[ReferenceData]] => PartialFunction[GlobalEvent, Outcome[
        ActorPool[ReferenceData, Performer[ReferenceData]]
      ]]
  ): StageManager[GameModel, ReferenceData] =
    this.copy(_updateActorPool = f)

object StageManager:

  private def noUpdate[ReferenceData]: ActorPool[ReferenceData, Performer[ReferenceData]] => PartialFunction[
    GlobalEvent,
    Outcome[ActorPool[ReferenceData, Performer[ReferenceData]]]
  ] =
    _ => PartialFunction.empty

  def apply[GameModel, ActorType](
      id: SubSystemId
  ): StageManager[GameModel, GameModel] =
    StageManager(id, None, identity, Batch.empty, noUpdate[GameModel])

  def apply[GameModel, ActorType](
      id: SubSystemId,
      layerKey: LayerKey
  ): StageManager[GameModel, GameModel] =
    StageManager(id, Some(layerKey), identity, Batch.empty, noUpdate[GameModel])

  def apply[GameModel, ReferenceData, ActorType](
      id: SubSystemId,
      layerKey: LayerKey,
      extractReference: GameModel => ReferenceData
  ): StageManager[GameModel, ReferenceData] =
    StageManager(id, Some(layerKey), extractReference, Batch.empty, noUpdate[ReferenceData])
