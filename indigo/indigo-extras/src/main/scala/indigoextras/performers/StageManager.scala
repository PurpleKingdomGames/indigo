package indigoextras.performers

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigo.shared.scenegraph.LayerKey
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.subsystems.SubSystemId
import indigoextras.actors.*

/*

What am I doing?

1. DONE ~~ Restoring a sub system version of Actors, initially just with one ActorPool
  1. DONE ~~ spawn / kill via events
  2. DONE ~~ find via context (supplied from ActorSystem, not ActorPool)
  3. DONE ~~ Dumber depth management
2. DONE ~~ Tree of pools? Or just a list?
  1. DONE ~~ Events to reorder / reparent
  2. WONTDO ~~ Trees render to layers
3. DONE ~~ Types of performers
  1. DONE ~~ Lead
  2. DONE ~~ Extra
  3. DONE ~~ Narrator
4. Physics out-of-the-box (Stunt)
  1. Might be another actor type?

Testing is needed.

 */

final case class StageManager[GameModel, RefData](
    id: SubSystemId,
    extractReference: GameModel => RefData
) extends SubSystem[GameModel]:

  type EventType      = GlobalEvent
  type SubSystemModel = StageManager.Model[ReferenceData]
  type ReferenceData  = RefData

  private given Actor[ReferenceData, Performer[ReferenceData]] =
    new Actor[ReferenceData, Performer[ReferenceData]]:
      def update(
          context: ActorContext[ReferenceData, Performer[ReferenceData]],
          performer: Performer[ReferenceData]
      ): GlobalEvent => Outcome[Performer[ReferenceData]] =
        case FrameTick =>
          performer match
            case p: Performer.Narrator[ReferenceData] =>
              p.update(PerformerContext.fromActorContext(context))(FrameTick)

            case p: Performer.Extra[ReferenceData] =>
              Outcome(p.update(PerformerContext.fromActorContext(context)))

            case p: Performer.Lead[ReferenceData] =>
              p.update(PerformerContext.fromActorContext(context))(FrameTick)

        case e =>
          performer match
            case p: Performer.Narrator[ReferenceData] =>
              p.update(PerformerContext.fromActorContext(context))(e)

            case p: Performer.Extra[ReferenceData] =>
              Outcome(p)

            case p: Performer.Lead[ReferenceData] =>
              p.update(PerformerContext.fromActorContext(context))(e)

      def present(
          context: ActorContext[ReferenceData, Performer[ReferenceData]],
          performer: Performer[ReferenceData]
      ): Outcome[Batch[SceneNode]] =
        performer match
          case p: Performer.Narrator[ReferenceData] =>
            Outcome(Batch.empty)

          case p: Performer.Extra[ReferenceData] =>
            Outcome(Batch(p.present(PerformerContext.fromActorContext(context))))

          case p: Performer.Lead[ReferenceData] =>
            p.present(PerformerContext.fromActorContext(context))

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: GameModel): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[SubSystemModel] =
    Outcome(StageManager.Model.empty[ReferenceData])

  def update(
      context: SubSystemContext[ReferenceData],
      model: StageManager.Model[ReferenceData]
  ): GlobalEvent => Outcome[StageManager.Model[ReferenceData]] = {
    case e: PerformerEvent =>
      handlePerformerEvents(model)(e)

    case e =>
      model.update(context, context.reference)(e)
  }

  private def handlePerformerEvents(
      model: StageManager.Model[ReferenceData]
  ): PerformerEvent => Outcome[StageManager.Model[ReferenceData]] = {
    case PerformerEvent.Add(layerKey, performer) =>
      Outcome(
        model.spawn(layerKey, Batch(performer.asInstanceOf[Performer[ReferenceData]]))
      )

    case PerformerEvent.AddAll(layerKey, performers) =>
      Outcome(
        model.spawn(layerKey, performers.map(_.asInstanceOf[Performer[ReferenceData]]))
      )

    case PerformerEvent.Remove(id) =>
      Outcome(
        model.kill(_.id == id)
      )

    case PerformerEvent.RemoveFrom(layerKey, id) =>
      Outcome(
        model.kill(layerKey, _.id == id)
      )

    case PerformerEvent.RemoveAll(ids) =>
      Outcome(
        model.kill(p => ids.exists(_ == p.id))
      )

    case PerformerEvent.RemoveAllFrom(layerKey, ids) =>
      Outcome(
        model.kill(layerKey, p => ids.exists(_ == p.id))
      )

    case PerformerEvent.ChangeLayer(id, layerKey) =>
      Outcome(
        model.changeLayers(id, layerKey)
      )
  }

  def present(
      context: SubSystemContext[ReferenceData],
      model: StageManager.Model[ReferenceData]
  ): Outcome[SceneUpdateFragment] =
    model.present(context, context.reference).map { layers =>
      SceneUpdateFragment(
        layers.map((layerKey, nodes) => layerKey -> Layer.Content(nodes))
      )
    }

  def withId(id: SubSystemId): StageManager[GameModel, ReferenceData] =
    this.copy(id = id)

object StageManager:

  private given [ReferenceData] => Ordering[Performer[ReferenceData]] =
    Ordering.by(_.depth.value)

  def apply[GameModel](
      id: SubSystemId
  ): StageManager[GameModel, GameModel] =
    StageManager(id, identity[GameModel])

  final case class Model[ReferenceData](pools: Map[LayerKey, ActorPool[ReferenceData, Performer[ReferenceData]]])(using
      actor: Actor[ReferenceData, Performer[ReferenceData]]
  ):

    def findById(id: PerformerId): Option[Performer[ReferenceData]] =
      pools.values.flatMap(_.find(_.id == id)).headOption

    def changeLayers(id: PerformerId, layerKey: LayerKey): Model[ReferenceData] =
      findById(id) match
        case None =>
          this

        case Some(performer) =>
          kill(_.id == id).spawn(layerKey, Batch(performer))

    def spawn(
        layerKey: LayerKey,
        newPerformers: Batch[Performer[ReferenceData]]
    ): Model[ReferenceData] =
      this.copy(
        pools = pools.updatedWith(layerKey) {
          case Some(existing) => Option(existing.spawn(newPerformers))
          case None           => Option(ActorPool.empty[ReferenceData, Performer[ReferenceData]].spawn(newPerformers))
        }
      )

    def kill(p: Performer[ReferenceData] => Boolean): Model[ReferenceData] =
      this.copy(pools = pools.view.mapValues(_.kill(p)).toMap)

    def kill(layerKey: LayerKey, p: Performer[ReferenceData] => Boolean): Model[ReferenceData] =
      this.copy(pools = pools.map { (k, pl) =>
        if k == layerKey then k -> pl.kill(p)
        else k                  -> pl
      })

    def update(
        context: SubSystemContext[?],
        model: ReferenceData
    ): GlobalEvent => Outcome[Model[ReferenceData]] = e =>
      val updated =
        Batch.fromMap(pools).map((k, p) => p.update(context, model)(e).map(p => k -> p)).sequence

      updated.map(next => this.copy(pools = next.toMap))

    def present(
        context: SubSystemContext[?],
        model: ReferenceData
    ): Outcome[Batch[(LayerKey, Batch[SceneNode])]] =
      Batch
        .fromMap(pools)
        .map { (k, p) =>
          p.present(context, model).map(ns => k -> ns)
        }
        .sequence

  object Model:
    def empty[ReferenceData](using actor: Actor[ReferenceData, Performer[ReferenceData]]): Model[ReferenceData] =
      Model(Map.empty[LayerKey, ActorPool[ReferenceData, Performer[ReferenceData]]])
