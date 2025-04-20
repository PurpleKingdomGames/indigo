package example

import indigo.*
import indigo.physics.*

object View:

  def present(world: World[MyTag]): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        world.present {
          case c: Collider.Circle[MyTag] =>
            Shape.Circle(
              c.bounds.position.toPoint,
              c.bounds.radius.toInt,
              Fill.Color(RGBA.White.withAlpha(0.2)),
              Stroke(1, RGBA.White)
            )

          case c: Collider.Box[MyTag] =>
            Shape.Box(
              c.bounds.toRectangle,
              Fill.Color(RGBA.White.withAlpha(0.2)),
              Stroke(1, RGBA.White)
            )
        }
      )
    )
