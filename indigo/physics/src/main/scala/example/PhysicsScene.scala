package example

import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*

object BallsScene extends PhysicsScene:

  val name: SceneName =
    SceneName("balls")

  val modelLens: Lens[Model, World[MyTag]] =
    Lens(_.balls, (m, w) => m.copy(balls = w))

  def world(dice: Dice): World[MyTag] =
    val circles =
      (0 to 8).toBatch.map { i =>
        Collider(
          MyTag.StaticCircle,
          BoundingCircle(i.toDouble * 100 + 30, 200.0, 20.0)
        ).makeStatic
      }

    val balls =
      (0 to 15).toBatch.map { i =>
        Collider(
          MyTag.Ball,
          BoundingCircle(i.toDouble * 50 + 28, (if i % 2 == 0 then 20 else 40) + i.toDouble * 2, 15.0)
        )
          .withRestitution(Restitution(0.8))
      }

    World
      .empty[MyTag]
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(circles)
      .addColliders(
        Collider(MyTag.Ball, BoundingCircle(400.0, 80.0, 25.0))
          .withRestitution(Restitution(0.6)),
        Collider(MyTag.Platform, BoundingCircle(-100.0d, 700.0, 300.0)).makeStatic,
        Collider(MyTag.Platform, BoundingCircle(900.0d, 700.0, 300.0)).makeStatic,
        Collider(MyTag.Platform, BoundingCircle(400.0d, 800.0, 300.0)).makeStatic
      )
      .addColliders(balls)

object BoxesScene extends PhysicsScene:

  val name: SceneName =
    SceneName("boxes")

  val modelLens: Lens[Model, World[MyTag]] =
    Lens(_.boxes, (m, w) => m.copy(boxes = w))

  def world(dice: Dice): World[MyTag] =
    val cubes =
      (0 to 10).toBatch.map { i =>
        Collider(MyTag.Box, BoundingBox(i.toDouble * 50 + 200, (if i % 2 == 0 then 0 else 50) + 60, 40, 40))
          .withRestitution(Restitution(0.6))
          .withVelocity(dice.roll(200) - 100, -dice.roll(350))
      }

    World.empty[MyTag]
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(
        Collider(MyTag.Platform, BoundingBox(10.0d, 550.0, 780.0, 20.0)).makeStatic
      )
      .addColliders(cubes)

object BoxesAndBallsScene extends PhysicsScene:

  val name: SceneName =
    SceneName("boxes and balls")

  val modelLens: Lens[Model, World[MyTag]] =
    Lens(_.boxesAndBalls, (m, w) => m.copy(boxesAndBalls = w))

  def world(dice: Dice): World[MyTag] =
    val circles =
      (0 to 8).toBatch.map { i =>
        Collider(MyTag.StaticCircle, BoundingCircle(i.toDouble * 100 + 30, 200.0, 20.0)).makeStatic
      }

    val cubes =
      (0 to 5).toBatch.map { i =>
        Collider(MyTag.Box, BoundingBox(i.toDouble * 90 + 200, (if i % 2 == 0 then 0 else 50) + 60, 40, 40))
          .withRestitution(Restitution(0.4))
          .withVelocity(dice.roll(200) - 100, -dice.roll(200))
      }

    val balls =
      (0 to 15).toBatch.map { i =>
        Collider(
          MyTag.Ball,
          BoundingCircle(i.toDouble * 50 + 28, (if i % 2 == 0 then 20 else 40) + i.toDouble * 2, 15.0)
        )
          .withRestitution(Restitution(0.7))
      }

    World.empty[MyTag]
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(
        Collider(MyTag.Ball, BoundingCircle(400.0, 80.0, 25.0)).withRestitution(Restitution(0.8)),
        Collider(MyTag.Platform, BoundingBox(10.0d, 550.0, 780.0, 20.0)).makeStatic
      )
      .addColliders(cubes)
      .addColliders(balls)
      .addColliders(circles)

sealed trait PhysicsScene extends Scene[Unit, Model, Unit]:

  def world(dice: Dice): World[MyTag]

  type SceneModel     = World[MyTag]
  type SceneViewModel = Unit

  val name: SceneName

  val modelLens: Lens[Model, World[MyTag]]

  val viewModelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: SceneContext[Unit],
      world: World[MyTag]
  ): GlobalEvent => Outcome[World[MyTag]] =
    case FrameTick =>
      world.update(context.delta)

    case _ =>
      Outcome(world)

  def updateViewModel(
      context: SceneContext[Unit],
      world: World[MyTag],
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[Unit],
      world: World[MyTag],
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    View.present(world)
