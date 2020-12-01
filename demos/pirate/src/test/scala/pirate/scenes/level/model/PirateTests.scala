package pirate.scenes.level.model

import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.datatypes.Vector2
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

class PirateTests extends munit.FunSuite {

  test("The Pirate.Input Mappings.run left and jump") {
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

    assertEquals(actual, expected)
  }

  test("The Pirate.Input Mappings.run right") {
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

    assertEquals(actual, expected)
  }

  test("The Pirate.Input Mappings.jump") {
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

    assertEquals(actual, expected)
  }

  test("The Pirate.Adjust position on collision.no collision") {

    val platform =
      Platform(List(BoundingBox(2, 8, 2, 1)), 10)

    val bounds =
      BoundingBox(0, 0, 1, 1)

    val actual =
      Pirate.adjustOnCollision(platform, bounds)

    val expected =
      bounds

    assertEquals(actual._1, expected)
    assertEquals(actual._2, false)

  }

  test("The Pirate.Adjust position on collision.collision") {

    val platform =
      Platform(List(BoundingBox(2, 8, 2, 1)), 10)

    val bounds =
      BoundingBox(0, 0, 1, 1).moveTo(Vertex(3.5, 7.5))

    val actual =
      Pirate.adjustOnCollision(platform, bounds)

    val expected =
      bounds.moveTo(Vertex(3.5, 7.0))

    assertEquals(actual._1, expected)
    assertEquals(actual._2, true)

  }

  val platformY: Double = 10.0d

  test("The Pirate.Decide next Y speed.on a platform (idle)") {
    assertEquals(Pirate.decideNextSpeedY(false, platformY, platformY, 0, 0), Pirate.gravityIncrement)
  }

  test("The Pirate.Decide next Y speed.on a platform (jump pressed)") {
    val inputY = -8.0d

    assertEquals(
      Pirate.decideNextSpeedY(
        false,
        platformY,
        platformY,
        0,
        inputY
      ),
      Pirate.gravityIncrement + inputY
    )
  }

  test("The Pirate.Decide next Y speed.accelerating during fall") {
    val ySpeed = 5.0d

    assertEquals(
      Pirate.decideNextSpeedY(
        true,
        platformY,
        platformY + ySpeed,
        ySpeed,
        0
      ),
      ySpeed + Pirate.gravityIncrement
    )
  }

  test("The Pirate.Decide next Y speed.terminal velocity") {
    val ySpeed = 8.0d

    assertEquals(
      Pirate.decideNextSpeedY(
        true,
        platformY,
        platformY + ySpeed,
        ySpeed,
        0
      ),
      ySpeed
    )
  }

}
