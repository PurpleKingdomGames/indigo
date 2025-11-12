package example

import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*

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

    World(SimulationSettings(BoundingBox(0, 0, 800, 600)).withMaxIterations(8))
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(
        Collider(MyTag.Ball, BoundingCircle(400.0, 80.0, 25.0)).withRestitution(Restitution(0.8)),
        Collider(MyTag.Platform, BoundingBox(10.0d, 550.0, 780.0, 20.0)).makeStatic
      )
      .addColliders(cubes)
      .addColliders(balls)
      .addColliders(circles)
