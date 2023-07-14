package pirate.scenes.level.model

import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.datatypes.Vector2
import indigo.shared.constants.Key
import indigo.shared.events.KeyboardEvent.KeyDown
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex
import indigo.shared.collections.Batch

class PirateTests extends munit.FunSuite {

  test("The Pirate.Input Mappings.run left and jump") {
    val keyboard =
      Keyboard.calculateNext(
        Keyboard.default,
        Batch(KeyDown(Key.LEFT_ARROW), KeyDown(Key.SPACE))
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
      Some(Vector2(-4.0d, -10.0d))

    assertEquals(actual, expected)
  }

  test("The Pirate.Input Mappings.run right") {
    val keyboard =
      Keyboard.calculateNext(
        Keyboard.default,
        Batch(KeyDown(Key.RIGHT_ARROW))
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
      Some(Vector2(4.0d, 0.0d))

    assertEquals(actual, expected)
  }

  test("The Pirate.Input Mappings.jump") {
    val keyboard =
      Keyboard.calculateNext(
        Keyboard.default,
        Batch(KeyDown(Key.SPACE))
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
      Some(Vector2(0.0d, -10.0d))

    assertEquals(actual, expected)
  }

}
