package indigoextras.performers

import indigo.physics.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Vector2
import indigo.shared.events.FrameTick
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
    extractReference: GameModel => RefData,
    worldOptions: WorldOptions
) extends SubSystem[GameModel]:

  type EventType      = GlobalEvent
  type SubSystemModel = StageManager.Model[ReferenceData]
  type ReferenceData  = RefData

  def eventFilter: GlobalEvent => Option[GlobalEvent] =
    e => Some(e)

  def reference(model: GameModel): ReferenceData =
    extractReference(model)

  def initialModel: Outcome[SubSystemModel] =
    Outcome(StageManager.Model[ReferenceData](worldOptions))

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

          case p: Performer.Lead[?] =>
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
          performers.collect {
            case p: Performer.Stunt[?] => p.initialCollider
            case p: Performer.Lead[?]  => p.initialCollider
          }
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

  /** Sets the inital physics world / simulation options */
  def withWorldOptions(
      value: WorldOptions
  ): StageManager[GameModel, ReferenceData] =
    this.copy(worldOptions = value)

  /** Sets the inital physics world / simulation options */
  def withWorldOptions(
      forces: Batch[Vector2],
      resistance: Resistance,
      settings: SimulationSettings
  ): StageManager[GameModel, ReferenceData] =
    withWorldOptions(WorldOptions(forces, resistance, settings))

object StageManager:

  def apply[GameModel](
      id: SubSystemId
  ): StageManager[GameModel, GameModel] =
    StageManager(id, identity[GameModel], WorldOptions.default)

  def apply[GameModel, ReferenceData](
      id: SubSystemId,
      extractReference: GameModel => ReferenceData
  ): StageManager[GameModel, ReferenceData] =
    StageManager(id, extractReference, WorldOptions.default)

  final case class Model[ReferenceData](
      pools: Map[LayerKey, PerformerPool[ReferenceData]],
      world: World[PerformerId]
  ):

    def findById(id: PerformerId): Option[Performer[ReferenceData]] =
      pools.values.flatMap(_.find(_.id == id)).headOption

    def findColliderById(id: PerformerId): Option[Collider[PerformerId]] =
      world.findByTag(id).headOption

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
          case None           => Option(PerformerPool[ReferenceData](findColliderById).spawn(newPerformers))
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
    ): GlobalEvent => Outcome[Model[ReferenceData]] =
      case FrameTick =>
        val ctx =
          PerformerContext(
            findById,
            findColliderById,
            model,
            context
          )

        Batch
          .fromMap(pools)
          .map((k, p) => p.update(context, model)(FrameTick).map(p => k -> p))
          .sequence
          .flatMap { updatedPools =>
            val updatedWorld =
              val physicalPerformers =
                updatedPools
                  .map(_._2)
                  .flatMap(
                    _.performers.filter(_.hasCollider)
                  )

              physicalPerformers
                .foldLeft(world) { (w, p) =>
                  w.modifyByTag(p.id) { c =>
                    p match
                      case p: Performer.Stunt[?] =>
                        p.updateCollider(ctx, c)

                      case p: Performer.Lead[?] =>
                        p.updateCollider(ctx, c)

                      case _ =>
                        c
                  }
                }
                .update(context.frame.time.delta)

            updatedWorld.map(_ -> updatedPools)
          }
          .map((nextWorld, nextModel) => this.copy(pools = nextModel.toMap, world = nextWorld))

      case e =>
        val updatedPools =
          Batch.fromMap(pools).map((k, p) => p.update(context, model)(e).map(p => k -> p)).sequence

        updatedPools
          .map(nextModel => this.copy(pools = nextModel.toMap))

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
        .map { layers =>
          layers.filter(_._2.nonEmpty)
        }

    def withWorld(world: World[PerformerId]): Model[ReferenceData] =
      this.copy(world = world)

  object Model:
    def apply[ReferenceData](worldOption: WorldOptions): Model[ReferenceData] =
      Model(
        Map.empty[LayerKey, PerformerPool[ReferenceData]],
        World(Batch.empty, worldOption.forces, worldOption.resistance, worldOption.settings)
      )
