package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.actors.*

object ActorPoolPhysicsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = ActorPhysicsSceneModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, ActorPhysicsSceneModel] =
    Lens(
      model => model.actorPhysicsScene,
      (model, sceneModel) => model.copy(actorPhysicsScene = sceneModel)
    )

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("actor pool physics scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ActorPhysicsSceneModel
  ): GlobalEvent => Outcome[ActorPhysicsSceneModel] =
    case FrameTick if !model.spawned =>
      val zombies: Batch[(ZombieActor, Collider[ZombieSimTag])] =
        (0 to 30).toBatch.map { i =>
          val dice = Dice.fromSeed(i.toLong)

          val x =
            dice.roll(context.startUpData.gameViewport.width / 2)
          val y =
            dice.roll(context.startUpData.gameViewport.height / 2)

          val acceleration: Double = dice.roll(40) + 10

          val zombie =
            ZombieActor(
              i,
              Point.zero,
              acceleration
            )

          val collider =
            Collider(
              ZombieSimTag.Zombie(i),
              BoundingCircle(x.toDouble, y.toDouble, 5.toDouble)
            )
              .withRestitution(Restitution(0.5))

          (zombie, collider)
        }

      val colliders =
        zombies.map(_._2)

      Outcome(
        model.copy(
          spawned = true,
          actorPool = zombies.map(_._1).foldLeft(model.actorPool) { (system, follower) =>
            system.spawn(follower)
          },
          world = model.world.withColliders(colliders)
        )
      )

    case FrameTick =>

      val target =
        model.target.toVertex

      model.world
        .modifyAll { collider =>
          collider match
            case c: Collider.Circle[ZombieSimTag] =>
              c.tag match
                case ZombieSimTag.Zombie(index) =>
                  val toTarget     = (target - c.bounds.position).toVector2.normalise
                  val acceleration = model.actorPool.find(_.index == index).map(_.acceleration).getOrElse(0.0)

                  c.withVelocity(c.velocity + (toTarget * acceleration))

                case ZombieSimTag.Target =>
                  c

            case c =>
              c
        }
        .update(context.frame.time.delta)(
          Collider.Circle(ZombieSimTag.Target, BoundingCircle(model.target.toVertex, 16)).makeStatic
        )
        .flatMap { updatedWorld =>

          val positionLookup: Map[Int, Point] =
            updatedWorld.collect { case c @ Collider.Circle(ZombieSimTag.Zombie(index)) =>
              index -> c.bounds.position.toPoint
            }.toMap

          model.actorPool
            .update(context.context, positionLookup)(FrameTick)
            .map { system =>
              model.copy(
                target = context.frame.input.mouse.position,
                actorPool = system,
                world = updatedWorld
              )
            }
        }

    case e =>
      model.actorPool
        .update(context.context, Map.empty)(e)
        .map { system =>
          model.copy(actorPool = system)
        }

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: ActorPhysicsSceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: ActorPhysicsSceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    model.actorPool.present(context.context, Map.empty).map { zombies =>
      SceneUpdateFragment(
        Constants.LayerKeys.background ->
          Layer.Content(
            Shape.Circle(Circle(model.target, 16), Fill.Color(RGBA.Red), Stroke(2, RGBA.White))
          ),
        Constants.LayerKeys.game -> Layer.Content(
          zombies
        )
      )
    }

final case class ActorPhysicsSceneModel(
    spawned: Boolean,
    target: Point,
    actorPool: ActorPool[Map[Int, Point], ZombieActor],
    world: World[ZombieSimTag]
)
object ActorPhysicsSceneModel:
  val initial: ActorPhysicsSceneModel =
    ActorPhysicsSceneModel(
      false,
      Point.zero,
      ActorPool.empty,
      World.empty.withResistance(Resistance(0.25))
    )

final case class ZombieActor(index: Int, position: Point, acceleration: Double):
  val depth: Int = 0

  def moveTo(newPosition: Point): ZombieActor =
    this.copy(position = newPosition)

object ZombieActor:

  given Ordering[ZombieActor] =
    Ordering.by(_.depth)

  given Actor[Map[Int, Point], ZombieActor] with

    def update(
        context: ActorContext[Map[Int, Point], ZombieActor],
        actor: ZombieActor
    ): GlobalEvent => Outcome[ZombieActor] =
      case FrameTick =>
        Outcome(
          context.reference
            .get(actor.index)
            .map { pos =>
              actor.moveTo(pos)
            }
            .getOrElse(actor)
        )

      case _ =>
        Outcome(actor)

    def present(context: ActorContext[Map[Int, Point], ZombieActor], actor: ZombieActor): Outcome[Batch[SceneNode]] =
      val color =
        actor.index % 3 match
          case 0 => RGBA.Cyan
          case 1 => RGBA.Yellow
          case _ => RGBA.SlateGray

      Outcome(
        Batch(
          Shape.Circle(Circle(actor.position, 5), Fill.Color(color), Stroke(1, RGBA.White))
        )
      )

enum ZombieSimTag derives CanEqual:
  case Zombie(index: Int)
  case Target

  def isZombie: Boolean =
    this match
      case Zombie(_) => true
      case Target    => false
