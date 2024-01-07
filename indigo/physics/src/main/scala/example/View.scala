package example

import indigo.*
import indigo.physics.*

object View:

  def present(world: World[MyTag]): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        world.present {
          case Collider.Circle(_, bounds, _, _, _, _, _, _, _, _) =>
            Shape.Circle(
              bounds.position.toPoint,
              bounds.radius.toInt,
              Fill.Color(RGBA.White.withAlpha(0.2)),
              Stroke(1, RGBA.White)
            )

          case Collider.Box(_, bounds, _, _, _, _, _, _, _, _) =>
            Shape.Box(
              bounds.toRectangle,
              Fill.Color(RGBA.White.withAlpha(0.2)),
              Stroke(1, RGBA.White)
            )
        }
      )
    )
