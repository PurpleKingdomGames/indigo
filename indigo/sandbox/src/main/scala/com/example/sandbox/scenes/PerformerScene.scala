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
    SceneName("performer scene")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set(
      StageManager[SandboxGameModel, Point](
        SubSystemId("performer system"),
        _.performerSceneModel.target
      )
    )

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: PerformerSceneModel
  ): GlobalEvent => Outcome[PerformerSceneModel] =
    case FrameTick if !model.spawned =>

      val followers: Batch[Follower] =
        (0 to 10).toBatch.map { i =>
          val dice  = Dice.fromSeed(i.toLong)
          val depth = 10 - i

          Follower(
            i,
            depth,
            Vertex.zero,
            dice.roll(15),
            0.1 + (dice.rollDouble * 0.9),
            dice.roll(30)
          )
        }

      Outcome(model.copy(spawned = true))
        .addGlobalEvents(PerformerEvent.Add(Constants.LayerKeys.game, Player.initial))
        .addGlobalEvents(PerformerEvent.AddAll(Constants.LayerKeys.game, followers))

    case FrameTick =>
      val orbit =
        Signal.SmoothPulse
          .affectTime(2.0)
          .flatMap { d =>
            Signal.Orbit(context.startUpData.viewportCenter, (100 * d) + 25)
          }
          .at(context.frame.time.running * 0.5)
          .toPoint

      Outcome(
        model.copy(target = orbit)
      )

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
        Constants.LayerKeys.background ->
          Layer.Content(
            Shape.Circle(Circle(model.target, 8), Fill.Color(RGBA.Cyan))
          ),
        Constants.LayerKeys.game ->
          Layer.Stack.empty
      )
    )

final case class PerformerSceneModel(spawned: Boolean, target: Point)
object PerformerSceneModel:
  val initial: PerformerSceneModel =
    PerformerSceneModel(false, Point.zero)

// -- Player --

final case class Player(position: Vector2, direction: Radians, trail: Batch[Breadcrumb], lastDropped: Seconds)
    extends Performer.Support[Point]:

  def id: PerformerId = PerformerId("player")

  def depth: PerformerDepth = PerformerDepth(1000)

  def update(context: PerformerContext[Point]): GlobalEvent => Outcome[Player] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(this)
        .addGlobalEvents(PerformerEvent.ChangeLayer(id, Constants.LayerKeys.background))

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

  def present(context: PerformerContext[Point]): Outcome[Batch[SceneNode]] =
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

// -- Follower --

final case class Follower(
    num: Int,
    depthIndex: Int,
    position: Vertex,
    divisor: Int,
    alpha: Double,
    size: Int
) extends Performer.Extra[Point]:

  def id: PerformerId       = PerformerId("follower" + num)
  def depth: PerformerDepth = PerformerDepth.zero

  def update(context: PerformerContext[Point]): Follower =
    val target =
      context
        .findById(PerformerId("follower" + (num - 1)))
        .collect { case Follower(_, _, p, _, _, _) => p }
        .getOrElse(context.reference.toVertex)
    val newPosition = position + ((target - position) / divisor)

    this.copy(position = newPosition)

  def present(context: PerformerContext[Point]): Batch[SceneNode] =
    Batch(
      Shape.Circle(Circle(position.toPoint, size), Fill.Color(RGBA.Green.withAlpha(alpha)), Stroke(2, RGBA.Black))
    )
