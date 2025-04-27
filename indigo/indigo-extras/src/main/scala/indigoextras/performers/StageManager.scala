package indigoextras.performers

import indigo.physics.*
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

final case class StageManager[GameModel, RefData](
    id: SubSystemId,
    extractReference: GameModel => RefData
) extends SubSystem[GameModel]:

  type EventType      = GlobalEvent
  type SubSystemModel = StageManager.Model[ReferenceData]
  type ReferenceData  = RefData

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
      val nextWorld =
        performer match
          case p: Performer.Stunt[?] =>
            model.world.addColliders(p.initialCollider)

          case _ =>
            model.world

      Outcome(
        model
          .spawn(layerKey, Batch(performer.asInstanceOf[Performer[ReferenceData]]))
          .withWorld(nextWorld)
      )

    case PerformerEvent.AddAll(layerKey, performers) =>
      val nextWorld =
        model.world.addColliders(
          performers.collect { case p: Performer.Stunt[?] => p.initialCollider }
        )

      Outcome(
        model
          .spawn(layerKey, performers.asInstanceOf[Batch[Performer[ReferenceData]]])
          .withWorld(nextWorld)
      )

    case PerformerEvent.Remove(id) =>
      val nextWorld =
        model.world.removeByTag(id)

      Outcome(
        model
          .kill(_.id == id)
          .withWorld(nextWorld)
      )

    case PerformerEvent.RemoveAll(ids) =>
      val nextWorld =
        model.world.removeAllByTag(ids)

      Outcome(
        model
          .kill(p => ids.exists(_ == p.id))
          .withWorld(nextWorld)
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

  def apply[GameModel](
      id: SubSystemId
  ): StageManager[GameModel, GameModel] =
    StageManager(id, identity[GameModel])

  final case class Model[ReferenceData](
      pools: Map[LayerKey, PerformerPool[ReferenceData]],
      world: World[PerformerId]
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
          case None           => Option(PerformerPool.empty[ReferenceData].spawn(newPerformers))
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
      val updatedWorld =
        world.update(context.frame.time.delta)

      val updatedPools =
        Batch.fromMap(pools).map((k, p) => p.update(context, model)(e).map(p => k -> p)).sequence

      updatedWorld
        .combine(updatedPools)
        .map((nextWorld, nextModel) => this.copy(pools = nextModel.toMap, world = nextWorld))

    def present(
        context: SubSystemContext[?],
        model: ReferenceData
    ): Outcome[Batch[(LayerKey, Batch[SceneNode])]] =
      val colliderLookup: PerformerId => Option[Collider[PerformerId]] =
        id => world.findByTag(id).headOption

      Batch
        .fromMap(pools)
        .map { (k, p) =>
          p.present(context, colliderLookup, model).map(ns => k -> ns)
        }
        .sequence

    def withWorld(world: World[PerformerId]): Model[ReferenceData] =
      this.copy(world = world)

  object Model:
    def empty[ReferenceData]: Model[ReferenceData] =
      Model(
        Map.empty[LayerKey, PerformerPool[ReferenceData]],
        World.empty
      )
