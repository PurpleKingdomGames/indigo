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

    World.empty
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
