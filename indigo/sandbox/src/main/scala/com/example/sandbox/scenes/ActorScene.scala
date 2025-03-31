package com.example.sandbox.scenes

import com.example.sandbox.Constants
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
      ActorSystem[SandboxGameModel, GameActor](
        SubSystemId("actor system"),
        Constants.LayerKeys.game
      )
        .updateActors { pool =>
          { case SpawnFollower(follower) =>
            Outcome(pool.spawn(follower))
          }
        }
        .spawn(
          GameActor.Player.initial
        )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSceneModel
  ): GlobalEvent => Outcome[ActorSceneModel] =
    case FrameTick if !model.spawned =>
      val followers: Batch[GameActor.Follower] =
        (0 to 10).toBatch.map { i =>
          val d = Dice.fromSeed(i.toLong)
          GameActor.Follower(
            ActorId("follower" + i),
            Vertex.zero,
            d.roll(50),
            0.05 + (d.rollDouble * 0.95),
            d.roll(32)
          )
        }

      Outcome(model.copy(spawned = true))
        .addGlobalEvents(
          followers.map(SpawnFollower.apply)
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
        Constants.LayerKeys.background ->
          Layer.Content(
            Shape.Circle(Circle(model.target, 8), Fill.Color(RGBA.Cyan))
          ),
        Constants.LayerKeys.game -> Layer.Stack.empty
      )
    )

final case class ActorSceneModel(spawned: Boolean, target: Point)
object ActorSceneModel:
  val initial: ActorSceneModel =
    ActorSceneModel(false, Point.zero)

enum GameActor:
  case Follower(id: ActorId, position: Vertex, divisor: Int, alpha: Double, size: Int)
  case Player(position: Vector2, direction: Radians, trail: Batch[Breadcrumb], lastDropped: Seconds)

object GameActor:

  object Follower:

    given Actor[SandboxGameModel, GameActor.Follower] with
      type ReferenceData = Point

      def reference(model: SandboxGameModel): Point =
        model.actorScene.target

      def depth(context: ActorContext[Point], actor: GameActor.Follower): Int =
        0

      def updateModel(
          context: ActorContext[Point],
          actor: GameActor.Follower
      ): GlobalEvent => Outcome[GameActor.Follower] =
        case FrameTick =>
          val target = context.reference
          Outcome(
            actor.copy(position = actor.position + ((target.toVertex - actor.position) / actor.divisor.toDouble))
          )

        case _ =>
          Outcome(actor)

      def present(
          context: ActorContext[Point],
          actor: GameActor.Follower
      ): Outcome[Batch[SceneNode]] =
        Outcome(
          Batch(
            Shape.Circle(Circle(actor.position.toPoint, actor.size), Fill.Color(RGBA.Magenta.withAlpha(actor.alpha)))
          )
        )

  object Player:
    val initial: GameActor.Player =
      Player(Vector2(135, 100), Radians.zero, Batch.empty, Seconds.zero)

    given Actor[SandboxGameModel, GameActor.Player] with
      type ReferenceData = Unit

      def reference(model: SandboxGameModel): Unit =
        ()

      def depth(context: ActorContext[Unit], actor: GameActor.Player): Int =
        0

      def updateModel(
          context: ActorContext[Unit],
          player: GameActor.Player
      ): GlobalEvent => Outcome[GameActor.Player] =
        case FrameTick =>
          val moveSpeed   = 2.0
          val rotateSpeed = 0.1

          def rotateLeft: Radians                 = player.direction - rotateSpeed
          def rotateRight: Radians                = player.direction + rotateSpeed
          def moveForward(dir: Radians): Vector2  = player.position + Vector2(0, -1).rotateTo(dir) * moveSpeed
          def moveBackward(dir: Radians): Vector2 = player.position - Vector2(0, -1).rotateTo(dir) * moveSpeed

          val (newPosition, newDirection) =
            context.frame.input.mapInputs(
              InputMapping(
                Combo.withKeyInputs(Key.ARROW_DOWN, Key.ARROW_LEFT) -> {
                  val dir = rotateLeft
                  (moveBackward(dir), dir)
                },
                Combo.withKeyInputs(Key.ARROW_DOWN, Key.ARROW_RIGHT) -> {
                  val dir = rotateRight
                  (moveBackward(dir), dir)
                },
                Combo.withKeyInputs(Key.ARROW_UP, Key.ARROW_LEFT) -> {
                  val dir = rotateLeft
                  (moveForward(dir), dir)
                },
                Combo.withKeyInputs(Key.ARROW_UP, Key.ARROW_RIGHT) -> {
                  val dir = rotateRight
                  (moveForward(dir), dir)
                },
                Combo.withKeyInputs(Key.ARROW_LEFT)  -> (player.position, rotateLeft),
                Combo.withKeyInputs(Key.ARROW_RIGHT) -> (player.position, rotateRight),
                Combo.withKeyInputs(Key.ARROW_UP)    -> (moveForward(player.direction), player.direction),
                Combo.withKeyInputs(Key.ARROW_DOWN)  -> (moveBackward(player.direction), player.direction)
              ),
              (player.position, player.direction)
            )

          val (maybeBreadcrumb, droppedAt) =
            if context.frame.time.running - player.lastDropped > Seconds(0.1) then
              (Batch(Breadcrumb(player.position.toPoint, context.frame.time.running)), context.frame.time.running)
            else (Batch.empty, player.lastDropped)

          Outcome(
            Player(
              newPosition,
              newDirection,
              player.trail.filter(b => (context.frame.time.running - b.droppedAt) < Seconds(2)) ++ maybeBreadcrumb,
              droppedAt
            )
          )

        case _ =>
          Outcome(player)

      def present(
          context: ActorContext[Unit],
          player: GameActor.Player
      ): Outcome[Batch[SceneNode]] =
        Outcome(
          player.trail.map { b =>
            Shape.Circle(
              Circle(b.position, 2),
              Fill.Color(RGBA.White.withAlpha(1.0 - ((context.frame.time.running - b.droppedAt).toDouble / 2.0)))
            )
          } ++
            Batch(
              Shape.Circle(Circle(player.position.toPoint, 20), Fill.Color(RGBA.Yellow)),
              Shape.Circle(
                Circle(player.position.toPoint + (Vector2(0, 1).rotateTo(player.direction) * 20).toPoint, 10),
                Fill.Color(RGBA.Yellow)
              ),
              Shape.Circle(
                Circle(player.position.toPoint + (Vector2(0, 1).rotateTo(player.direction - 0.5) * 15).toPoint, 4),
                Fill.Color(RGBA.Black)
              ),
              Shape.Circle(
                Circle(player.position.toPoint + (Vector2(0, 1).rotateTo(player.direction + 0.5) * 15).toPoint, 4),
                Fill.Color(RGBA.Black)
              )
            )
        )

final case class Breadcrumb(position: Point, droppedAt: Seconds)

final case class SpawnFollower(follower: GameActor.Follower) extends GlobalEvent
