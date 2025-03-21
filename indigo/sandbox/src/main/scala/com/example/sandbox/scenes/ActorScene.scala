package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.actors.*

object ActorScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = ActorSceneModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, ActorSceneModel] =
    Lens(
      model => model.actorScene,
      (model, sceneModel) => model.copy(actorScene = sceneModel)
    )

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("actor scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      ActorSystem(
        SubSystemId("actor system")
      )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSceneModel
  ): GlobalEvent => Outcome[ActorSceneModel] =
    case FrameTick if !model.spawned =>
      val followers =
        (0 to 10).toBatch.map { i =>
          val d = Dice.fromSeed(i.toLong)
          Follower(
            ActorId("follower" + i),
            Vertex.zero,
            d.roll(50),
            0.05 + (d.rollDouble * 0.95),
            d.roll(32)
          )
        }

      Outcome(model.copy(spawned = true))
        .addGlobalEvents(
          followers.map(ActorEvent.Spawn.apply)
        )

    case FrameTick =>
      val orbit = Signal.Orbit(Point(135, 100), 75).at(context.frame.time.running * 0.25)

      Outcome(
        model.copy(target = orbit.toPoint)
      )

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: ActorSceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Shape.Circle(Circle(model.target, 8), Fill.Color(RGBA.Cyan))
      )
    )

final case class ActorSceneModel(spawned: Boolean, target: Point)
object ActorSceneModel:
  val initial: ActorSceneModel =
    ActorSceneModel(false, Point.zero)

final case class Follower(id: ActorId, position: Vertex, divisor: Int, alpha: Double, size: Int)
    extends Actor[SandboxGameModel]:

  type ActorModel = Point

  def read(model: SandboxGameModel): Point = model.actorScene.target
  def depth(context: ActorContext, model: Point): Int = 0

  def updateModel(
      context: ActorContext,
      target: Point
  ): GlobalEvent => Outcome[Follower] =
    case FrameTick =>
      Outcome(
        this.copy(position = position + ((target.toVertex - position) / divisor.toDouble))
      )

    case _ =>
      Outcome(this)

  def present(
      context: ActorContext,
      target: Point
  ): Outcome[Batch[SceneNode]] =
    Outcome(
      Batch(
        Shape.Circle(Circle(position.toPoint, size), Fill.Color(RGBA.Magenta.withAlpha(alpha)))
      )
    )
