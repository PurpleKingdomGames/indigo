package com.example.sandbox.scenes

import com.example.sandbox.Constants
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.performers.*

object PerformerScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = PerformerSceneModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, PerformerSceneModel] =
    Lens(
      model => model.performerSceneModel,
      (model, sceneModel) => model.copy(performerSceneModel = sceneModel)
    )

  def viewModelLens: Lens[SandboxViewModel, Unit] =
    Lens.unit

  def name: SceneName =
    SceneName("actor sub system scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      StageManager[SandboxGameModel, Player](
        SubSystemId("actor system"),
        Constants.LayerKeys.game
      )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: PerformerSceneModel
  ): GlobalEvent => Outcome[PerformerSceneModel] =
    case FrameTick if !model.spawned =>
      Outcome(model.copy(spawned = true))
        .addGlobalEvents(PerformerEvent.Spawn(Player.initial))

    case _ =>
      Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: PerformerSceneModel,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: PerformerSceneModel,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Constants.LayerKeys.game -> Layer.Stack.empty
      )
    )

final case class PerformerSceneModel(spawned: Boolean)
object PerformerSceneModel:
  val initial: PerformerSceneModel =
    PerformerSceneModel(false)

final case class Player(position: Vector2, direction: Radians, trail: Batch[Breadcrumb], lastDropped: Seconds)
    extends Performer[SandboxGameModel]:

  def id: PerformerId = PerformerId("player")

  def depth: PerformerDepth = PerformerDepth(0)

  def update(context: PerformerContext[SandboxGameModel]): GlobalEvent => Outcome[Player] =
    case FrameTick =>
      val moveSpeed   = 2.0
      val rotateSpeed = 0.1

      def rotateLeft: Radians                 = direction - rotateSpeed
      def rotateRight: Radians                = direction + rotateSpeed
      def moveForward(dir: Radians): Vector2  = position + Vector2(0, -1).rotateTo(dir) * moveSpeed
      def moveBackward(dir: Radians): Vector2 = position - Vector2(0, -1).rotateTo(dir) * moveSpeed

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
            Combo.withKeyInputs(Key.ARROW_LEFT)  -> (position, rotateLeft),
            Combo.withKeyInputs(Key.ARROW_RIGHT) -> (position, rotateRight),
            Combo.withKeyInputs(Key.ARROW_UP)    -> (moveForward(direction), direction),
            Combo.withKeyInputs(Key.ARROW_DOWN)  -> (moveBackward(direction), direction)
          ),
          (position, direction)
        )

      val (maybeBreadcrumb, droppedAt) =
        if context.frame.time.running - lastDropped > Seconds(0.1) then
          (Batch(Breadcrumb(position.toPoint, context.frame.time.running)), context.frame.time.running)
        else (Batch.empty, lastDropped)

      Outcome(
        Player(
          newPosition,
          newDirection,
          trail.filter(b => (context.frame.time.running - b.droppedAt) < Seconds(2)) ++ maybeBreadcrumb,
          droppedAt
        )
      )

    case _ =>
      Outcome(this)

  def present(context: PerformerContext[SandboxGameModel]): Outcome[Batch[SceneNode]] =
    Outcome(
      trail.map { b =>
        Shape.Circle(
          Circle(b.position, 2),
          Fill.Color(RGBA.White.withAlpha(1.0 - ((context.frame.time.running - b.droppedAt).toDouble / 2.0)))
        )
      } ++
        Batch(
          Shape.Circle(Circle(position.toPoint, 20), Fill.Color(RGBA.Yellow)),
          Shape.Circle(
            Circle(position.toPoint + (Vector2(0, 1).rotateTo(direction) * 20).toPoint, 10),
            Fill.Color(RGBA.Yellow)
          ),
          Shape.Circle(
            Circle(position.toPoint + (Vector2(0, 1).rotateTo(direction - 0.5) * 15).toPoint, 4),
            Fill.Color(RGBA.Black)
          ),
          Shape.Circle(
            Circle(position.toPoint + (Vector2(0, 1).rotateTo(direction + 0.5) * 15).toPoint, 4),
            Fill.Color(RGBA.Black)
          )
        )
    )

object Player:
  val initial: Player =
    Player(Vector2(135, 100), Radians.zero, Batch.empty, Seconds.zero)

final case class Breadcrumb(position: Point, droppedAt: Seconds)
