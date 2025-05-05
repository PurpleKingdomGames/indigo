package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.performers.*

object PerformerPhysicsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = PerformerPhysicsSceneModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, PerformerPhysicsSceneModel] =
    Lens(
      model => model.performerPhysicsSceneModel,
      (model, sceneModel) => model.copy(performerPhysicsSceneModel = sceneModel)
    )

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("performer physics scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      StageManager[SandboxGameModel, Point](
        SubSystemId("performer system"),
        _.performerSceneModel.target
      ).withWorldOptions(
        WorldOptions.default
          .withForces(Vector2(0, 0))
          .withResistance(Resistance(0.25))
          .withSimulationSettings(SimulationSettings.default)
      )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: PerformerPhysicsSceneModel
  ): GlobalEvent => Outcome[PerformerPhysicsSceneModel] =
    case FrameTick if !model.spawned =>
      val zombies: Batch[ZombiePerformer] =
        (0 to 30).toBatch.map { i =>
          val dice = Dice.fromSeed(i.toLong)

          val x =
            dice.roll(context.startUpData.gameViewport.width / 2)
          val y =
            dice.roll(context.startUpData.gameViewport.height / 2)

          val acceleration: Double = dice.roll(40) + 10

          ZombiePerformer(
            i,
            Point(x, y),
            acceleration
          )
        }

      Outcome(model.copy(spawned = true))
        .addGlobalEvents(PerformerEvent.Add(Constants.LayerKeys.background, ZombieTargetPerformer()))
        .addGlobalEvents(PerformerEvent.AddAll(Constants.LayerKeys.game, zombies))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: PerformerPhysicsSceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: PerformerPhysicsSceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.background -> Layer.empty,
        Constants.LayerKeys.game       -> Layer.Stack.empty
      )
    )

final case class PerformerPhysicsSceneModel(spawned: Boolean)
object PerformerPhysicsSceneModel:
  val initial: PerformerPhysicsSceneModel =
    PerformerPhysicsSceneModel(false)

final case class ZombiePerformer(
    index: Int,
    position: Point,
    acceleration: Double
) extends Performer.Stunt[Point]:

  val radius = 5

  def id: PerformerId       = PerformerId("zombie-" + index)
  def depth: PerformerDepth = PerformerDepth.zero

  def initialCollider: Collider[PerformerId] =
    Collider
      .Circle(
        id,
        BoundingCircle(position.toVertex, radius.toDouble)
      )
      .withRestitution(Restitution(0.5))

  def update(context: PerformerContext[Point]): Performer.Stunt[Point] =
    this

  def updateCollider(context: PerformerContext[Point], collider: Collider[PerformerId]): Collider[PerformerId] =
    val target   = context.findColliderById(ZombieTargetPerformer.id).map(_.boundingBox.position).getOrElse(Vertex.zero)
    val toTarget = (target - collider.boundingBox.position).toVector2.normalise

    collider.withVelocity(collider.velocity + (toTarget * acceleration))

  def present(context: PerformerContext[Point], collider: Collider[PerformerId]): Batch[SceneNode] =
    val color =
      index % 3 match
        case 0 => RGBA.Cyan
        case 1 => RGBA.Yellow
        case _ => RGBA.SlateGray

    Batch(
      Shape.Circle(Circle(collider.position.toPoint, radius), Fill.Color(color), Stroke(1, RGBA.White))
    )

final case class ZombieTargetPerformer() extends Performer.Lead[Point]:
  def id: PerformerId       = ZombieTargetPerformer.id
  def depth: PerformerDepth = PerformerDepth.zero

  val radius = 16

  def initialCollider: Collider[PerformerId] =
    Collider
      .Circle(
        id,
        BoundingCircle(Point.zero.toVertex, radius)
      )
      .makeStatic

  def update(context: PerformerContext[Point]): GlobalEvent => Outcome[Performer.Lead[Point]] =
    _ => Outcome(this)

  def updateCollider(context: PerformerContext[Point], collider: Collider[PerformerId]): Collider[PerformerId] =
    collider.moveTo(context.frame.input.mouse.position.toVertex)

  def present(context: PerformerContext[Point], collider: Collider[PerformerId]): Outcome[Batch[SceneNode]] =
    Outcome(
      Batch(
        Shape.Circle(Circle(collider.position.toPoint, radius), Fill.Color(RGBA.Red), Stroke(1, RGBA.White))
      )
    )

object ZombieTargetPerformer:
  val id: PerformerId =
    PerformerId("zombie-target")
