package example

import indigo.*
import indigo.physics.*
import indigo.scenes.*
import indigo.syntax.*

object VolumeScene extends PhysicsScene:

  val name: SceneName =
    SceneName("volume scene")

  val modelLens: Lens[Model, World[MyTag]] =
    Lens(_.volume, (m, w) => m.copy(volume = w))

  def world(dice: Dice): World[MyTag] =
    val energy = 1000
    val r      = 5
    val size   = (r + 2) * 2
    val cols   = (500 - 40) / size

    val balls =
      (0 to 1000).toBatch.map { i =>
        Collider(
          MyTag.Ball,
          BoundingCircle(
            x = 50 + 20 + 15 + ((i % cols) * size),
            y = 50 + 20 + 15 + (Math.floor(i / cols) * size),
            radius = r
          )
        )
          .withVelocity(Vector2(Math.sin(i.toDouble) * energy, Math.cos(i.toDouble) * energy))
          .withTerminalVelocity(Vector2(100))
      }

    World(SimulationSettings(BoundingBox(50, 50, 500, 500)))
      .withColliders(
        Collider(MyTag.Platform, BoundingBox(50.0, 50.0, 500.0, 20.0)).makeStatic,
        Collider(MyTag.Platform, BoundingBox(500.0 + 50 - 20, 50.0, 20.0, 500.0)).makeStatic,
        Collider(MyTag.Platform, BoundingBox(50.0, 500.0 + 50 - 20, 500.0, 20.0)).makeStatic,
        Collider(MyTag.Platform, BoundingBox(50.0, 50.0, 20.0, 500.0)).makeStatic
      )
      .addColliders(balls)
