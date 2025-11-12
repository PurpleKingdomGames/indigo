package example

import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*

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

    World(SimulationSettings(BoundingBox(0, 0, 800, 600)).withMaxIterations(8))
      .addForces(Vector2(0, 600))
      .withResistance(Resistance(0.01))
      .withColliders(
        Collider(MyTag.Platform, BoundingBox(10.0d, 550.0, 780.0, 20.0)).makeStatic
      )
      .addColliders(cubes)
