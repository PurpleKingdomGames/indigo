package pirate.scenes.level.model

import utest._
import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.datatypes.Vector2
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

object PirateTests extends TestSuite {

  val tests: Tests =
    Tests {

      "The Pirate" - {

        "Input Mappings" - {

          "run left and jump" - {
            val keyboard =
              Keyboard.calculateNext(
                Keyboard.default,
                List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.SPACE))
              )

            val actual: Option[Vector2] =
              Pirate
                .inputMappings(false)
                .find(
                  Mouse.default,
                  keyboard,
                  Gamepad.default
                )

            val expected: Option[Vector2] =
              Some(Vector2(-3.0d, -8.0d))

            actual ==> expected
          }

          "run right" - {
            val keyboard =
              Keyboard.calculateNext(
                Keyboard.default,
                List(KeyDown(Key.RIGHT_ARROW))
              )

            val actual: Option[Vector2] =
              Pirate
                .inputMappings(false)
                .find(
                  Mouse.default,
                  keyboard,
                  Gamepad.default
                )

            val expected: Option[Vector2] =
              Some(Vector2(3.0d, 0.0d))

            actual ==> expected
          }

          "jump" - {
            val keyboard =
              Keyboard.calculateNext(
                Keyboard.default,
                List(KeyDown(Key.SPACE))
              )

            val actual: Option[Vector2] =
              Pirate
                .inputMappings(false)
                .find(
                  Mouse.default,
                  keyboard,
                  Gamepad.default
                )

            val expected: Option[Vector2] =
              Some(Vector2(0.0d, -8.0d))

            actual ==> expected
          }

        }

        "Adjust position on collision" - {

          "no collision" - {

            val platform =
              Platform(List(BoundingBox(2, 8, 2, 1)), 10)

            val bounds =
              BoundingBox(0, 0, 1, 1)

            val actual =
              Pirate.adjustOnCollision(platform, bounds)

            val expected =
              bounds

            actual._1 ==> expected
            actual._2 ==> false

          }

          "collision" - {

            val platform =
              Platform(List(BoundingBox(2, 8, 2, 1)), 10)

            val bounds =
              BoundingBox(0, 0, 1, 1).moveTo(Vertex(3.5, 7.5))

            val actual =
              Pirate.adjustOnCollision(platform, bounds)

            val expected =
              bounds.moveTo(Vertex(3.5, 7.0))

            actual._1 ==> expected
            actual._2 ==> true

          }

        }

        "Decide next Y speed" - {
          val platformY: Double = 10.0d

          "on a platform (idle)" - {
            Pirate.decideNextSpeedY(false, platformY, platformY, 0, 0) ==> Pirate.gravityIncrement
          }

          "on a platform (jump pressed)" - {
            val inputY = -8.0d

            Pirate.decideNextSpeedY(
              false,
              platformY,
              platformY,
              0,
              inputY
            ) ==> Pirate.gravityIncrement + inputY
          }

          "accelerating during fall" - {
            val ySpeed = 5.0d

            Pirate.decideNextSpeedY(
              true,
              platformY,
              platformY + ySpeed,
              ySpeed,
              0
            ) ==> ySpeed + Pirate.gravityIncrement
          }

          "terminal velocity" - {
            val ySpeed = 8.0d

            Pirate.decideNextSpeedY(
              true,
              platformY,
              platformY + ySpeed,
              ySpeed,
              0
            ) ==> ySpeed
          }

        }

      }

    }

}
