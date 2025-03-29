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
      ActorSystem[SandboxGameModel](
        SubSystemId("actor system"),
        Constants.LayerKeys.game
      )
        .updateActors { (_, pool) =>
          { case SpawnFollower(follower) =>
            Outcome(pool.spawn(follower))
          }
        }
        .spawn(
          PlayerActor.initial
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

final case class Follower(id: ActorId, position: Vertex, divisor: Int, alpha: Double, size: Int)
    extends Actor[SandboxGameModel]:

  type ReferenceData = Point

  def reference(model: SandboxGameModel): Point       = model.actorScene.target
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

final case class PlayerActor(position: Vector2, direction: Radians, trail: Batch[Breadcrumb], lastDropped: Seconds)
    extends Actor[SandboxGameModel]:

  def id: ActorId = ActorId("player")

  type ReferenceData = Unit

  def reference(model: SandboxGameModel): Unit           = ()
  def depth(context: ActorContext, reference: Unit): Int = 0

  def updateModel(
      context: ActorContext,
      reference: Unit
  ): GlobalEvent => Outcome[PlayerActor] =
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
        PlayerActor(
          newPosition,
          newDirection,
          trail.filter(b => (context.frame.time.running - b.droppedAt) < Seconds(2)) ++ maybeBreadcrumb,
          droppedAt
        )
      )

    case _ =>
      Outcome(this)

  def present(
      context: ActorContext,
      reference: Unit
  ): Outcome[Batch[SceneNode]] =
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
object PlayerActor:
  def initial: PlayerActor =
    PlayerActor(Vector2(135, 100), Radians.zero, Batch.empty, Seconds.zero)

final case class Breadcrumb(position: Point, droppedAt: Seconds)

final case class SpawnFollower(follower: Follower) extends GlobalEvent
