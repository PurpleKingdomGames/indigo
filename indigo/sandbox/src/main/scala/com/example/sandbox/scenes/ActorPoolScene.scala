package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.actors.*

object ActorPoolScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

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
    SceneName("actor pool scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSceneModel
  ): GlobalEvent => Outcome[ActorSceneModel] =
    case FrameTick if !model.spawned =>
      val followers: Batch[FollowingActor] =
        (0 to 10).toBatch.map { i =>
          val dice  = Dice.fromSeed(i.toLong)
          val depth = 10 - i

          if 0 == i % 2 then
            FollowingActor.YellowFollower(
              i,
              depth,
              Vertex.zero,
              dice.roll(12),
              0.1 + (dice.rollDouble * 0.9),
              dice.roll(80)
            )
          else
            FollowingActor.PinkFollower(
              i,
              depth,
              Vertex.zero,
              dice.roll(6),
              0.1 + (dice.rollDouble * 0.9),
              dice.roll(30)
            )
        }

      Outcome(
        model.copy(
          spawned = true,
          actorSystem = followers.foldLeft(model.actorSystem) { (system, follower) =>
            system.spawn(follower)
          }
        )
      )

    case FrameTick =>
      val orbit =
        Signal.SmoothPulse
          .affectTime(2.0)
          .flatMap { d =>
            Signal.Orbit(context.startUpData.viewportCenter, (100 * d) + 25)
          }
          .at(context.frame.time.running * 0.5)
          .toPoint

      model.actorSystem
        .update(context.context, orbit)(FrameTick)
        .map { system =>
          model.copy(actorSystem = system, target = orbit)
        }

    case e =>
      model.actorSystem
        .update(context.context, model.target)(e)
        .map { system =>
          model.copy(actorSystem = system)
        }

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
    model.actorSystem.present(context.context, model.target).map { followers =>
      SceneUpdateFragment(
        Constants.LayerKeys.background ->
          Layer.Content(
            Shape.Circle(Circle(model.target, 8), Fill.Color(RGBA.Cyan))
          ),
        Constants.LayerKeys.game -> Layer.Content(
          followers
        )
      )
    }

final case class ActorSceneModel(spawned: Boolean, target: Point, actorSystem: ActorPool[Point, FollowingActor])
object ActorSceneModel:
  val initial: ActorSceneModel =
    ActorSceneModel(
      false,
      Point.zero,
      ActorPool.empty
    )

enum FollowingActor(
    val index: Int,
    val depthIndex: Int,
    val location: Vertex,
    val radius: Int,
    val divisorValue: Double
):
  case PinkFollower(num: Int, depth: Int, position: Vertex, divisor: Int, alpha: Double, size: Int)
      extends FollowingActor(num, depth, position, size, divisor.toDouble)

  case YellowFollower(num: Int, depth: Int, position: Vertex, divisor: Int, alpha: Double, size: Int)
      extends FollowingActor(num, depth, position, size, divisor.toDouble)

  def moveTo(newPosition: Vertex): FollowingActor =
    this match
      case f: FollowingActor.PinkFollower =>
        f.copy(position = newPosition)

      case f: FollowingActor.YellowFollower =>
        f.copy(position = newPosition)

  def colour: RGBA =
    this match
      case FollowingActor.PinkFollower(_, _, _, _, alpha, _) =>
        RGBA.Magenta.withAlpha(alpha)

      case FollowingActor.YellowFollower(_, _, _, _, alpha, _) =>
        RGBA.Yellow.withAlpha(alpha)

object FollowingActor:

  given Ordering[FollowingActor] =
    Ordering.by(_.depthIndex)

  given Actor[Point, FollowingActor] with

    def update(
        context: ActorContext[Point, FollowingActor],
        actor: FollowingActor
    ): GlobalEvent => Outcome[FollowingActor] =
      case FrameTick =>
        val target =
          context.find(_.index == actor.index - 1) match
            case Some(follower) =>
              follower.location

            case None =>
              context.reference.toVertex

        Outcome(
          actor.moveTo(actor.location + ((target - actor.location) / actor.divisorValue))
        )

      case _ =>
        Outcome(actor)

    def present(
        context: ActorContext[Point, FollowingActor],
        actor: FollowingActor
    ): Outcome[Batch[SceneNode]] =
      Outcome(
        Batch(
          Shape.Circle(Circle(actor.location.toPoint, actor.radius), Fill.Color(actor.colour), Stroke(2, RGBA.Black))
        )
      )
