package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.actors.*

object ActorSubSystemScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = ActorSubSystemSceneModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, ActorSubSystemSceneModel] =
    Lens(
      _ => ActorSubSystemSceneModel(),
      (model, _) => model
    )

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("actor sub system scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      ActorSystem[SandboxGameModel, Player](
        SubSystemId("actor system"),
        Constants.LayerKeys.game
      )
        .spawn(
          Player.initial
        )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSubSystemSceneModel
  ): GlobalEvent => Outcome[ActorSubSystemSceneModel] =
    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: ActorSubSystemSceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: ActorSubSystemSceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.game -> Layer.Stack.empty
      )
    )

final case class ActorSubSystemSceneModel()
object ActorSubSystemSceneModel:
  val initial: ActorSubSystemSceneModel =
    ActorSubSystemSceneModel()

final case class Player(position: Vector2, direction: Radians, trail: Batch[Breadcrumb], lastDropped: Seconds)

object Player:
  val initial: Player =
    Player(Vector2(135, 100), Radians.zero, Batch.empty, Seconds.zero)

  given Ordering[Player] =
    Ordering.by(_ => 0)

  given Actor[SandboxGameModel, Player] with

    def updateModel(
        context: ActorContext[SandboxGameModel, Player],
        player: Player
    ): GlobalEvent => Outcome[Player] =
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
        context: ActorContext[SandboxGameModel, Player],
        player: Player
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
