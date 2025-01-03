package indigo.shared.events

import indigo.MouseButton
import indigo.shared.collections.Batch
import indigo.shared.constants.Key
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.input.AnalogAxis
import indigo.shared.input.Gamepad
import indigo.shared.input.GamepadAnalogControls
import indigo.shared.input.GamepadButtons
import indigo.shared.input.GamepadDPad

class InputStateTests extends munit.FunSuite {

  val bounds: Rectangle =
    Rectangle(10, 10, 100, 100)

  val inputState: InputState =
    InputState.default

  val gamepadState1: Gamepad =
    Gamepad.default

  test("The default state object does the expected thing") {
    assertEquals(inputState.mouse.isLeftDown, false)
    assertEquals(inputState.mouse.maybePosition, None)
    assertEquals(inputState.mouse.position, Point.zero)

    assertEquals(inputState.mouse.wasClickedWithin(bounds), false)
  }

  val events1: Batch[PointerEvent] =
    Batch(
      PointerEvent.Move(10, 10),
      PointerEvent.Down(10, 10),
      PointerEvent.Up(10, 10),
      PointerEvent.Click(10, 10)
    )

  val state = InputState.calculateNext(inputState, events1, gamepadState1)

  test("Mouse state.position") {
    assertEquals(state.mouse.position == Point(10, 10), true)
  }

  test("Mouse state.maybePosition") {
    assertEquals(state.mouse.maybePosition == Some(Point(10, 10)), true)
  }

  test("Mouse state.isPressed") {
    assertEquals(state.mouse.isPressed, true)
  }

  test("Mouse state.isReleased") {
    assertEquals(state.mouse.isReleased, true)
  }

  test("Mouse state.isClicked") {
    assertEquals(state.mouse.isClicked, true)

    assertEquals(
      InputState.calculateNext(inputState, Batch(PointerEvent.Down(0, 0)), gamepadState1).mouse.isClicked,
      false
    )
  }

  test("Mouse state.isClickedAt") {
    assertEquals(state.mouse.isClickedAt, Batch(Point(10, 10)))

    assertEquals(
      InputState.calculateNext(inputState, Batch(PointerEvent.Down(0, 0)), gamepadState1).mouse.isClickedAt,
      Batch.empty
    )
  }

  test("Mouse state.isUpAt") {
    assertEquals(state.mouse.isUpAt, Batch(Point(10, 10)))

    assertEquals(
      InputState.calculateNext(inputState, Batch(PointerEvent.Down(0, 0)), gamepadState1).mouse.isUpAt,
      Batch.empty
    )
  }

  test("Mouse state.isDownAt") {
    assertEquals(state.mouse.isDownAt, Batch(Point(10, 10)))

    assertEquals(
      InputState.calculateNext(inputState, Batch(PointerEvent.Up(0, 0)), gamepadState1).mouse.isDownAt,
      Batch.empty
    )
  }

  test("Mouse state.wasClickedAt") {
    assertEquals(state.mouse.wasClickedAt(10, 10), true)
    assertEquals(state.mouse.wasClickedAt(20, 10), false)
  }

  test("Mouse state.wasUpAt") {
    assertEquals(state.mouse.wasUpAt(10, 10), true)
    assertEquals(state.mouse.wasUpAt(20, 10), false)
  }

  test("Mouse state.wasDownAt") {
    assertEquals(state.mouse.wasDownAt(10, 10), true)
    assertEquals(state.mouse.wasDownAt(20, 10), false)
  }

  test("Mouse state.wasPositionAt") {
    assertEquals(state.mouse.wasPositionAt(Point.zero), false)
    assertEquals(state.mouse.wasPositionAt(Point(10, 10)), true)
  }

  test("Mouse state.wasClickedWithin") {
    assertEquals(state.mouse.wasClickedWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasClickedWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasClickedWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.wasUpWithin") {
    assertEquals(state.mouse.wasUpWithin(Rectangle(0, 0, 5, 5), MouseButton.LeftMouseButton), false)
    assertEquals(state.mouse.wasUpWithin(Rectangle(50, 50, 5, 5), MouseButton.LeftMouseButton), false)
    assertEquals(state.mouse.wasUpWithin(Rectangle(5, 5, 10, 10), MouseButton.LeftMouseButton), true)
  }

  test("Mouse state.wasDownWithin") {
    assertEquals(state.mouse.wasDownWithin(Rectangle(0, 0, 5, 5), MouseButton.LeftMouseButton), false)
    assertEquals(state.mouse.wasDownWithin(Rectangle(50, 50, 5, 5), MouseButton.LeftMouseButton), false)
    assertEquals(state.mouse.wasDownWithin(Rectangle(5, 5, 10, 10), MouseButton.LeftMouseButton), true)
  }

  test("Mouse state.wasWithin") {
    assertEquals(state.mouse.wasWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.isLeftDown") {

    val state2 = InputState.calculateNext(state, Batch(PointerEvent.Down(0, 0)), gamepadState1)    // true
    val state3 = InputState.calculateNext(state2, Batch.empty, gamepadState1)                      // still true
    val state4 = InputState.calculateNext(state3, Batch(PointerEvent.Down(20, 20)), gamepadState1) // still true
    val state5 = InputState.calculateNext( // Still true
      state4,
      Batch(PointerEvent.Up(20, 20), PointerEvent.Down(20, 20)),
      gamepadState1
    )
    val state6 = InputState.calculateNext(state5, Batch(PointerEvent.Up(20, 20)), gamepadState1) // false
    val state7 = InputState.calculateNext( // Still false
      state6,
      Batch(PointerEvent.Down(20, 20), PointerEvent.Up(20, 20)),
      gamepadState1
    )

    assertEquals(state.mouse.isLeftDown, false)
    assertEquals(state2.mouse.isLeftDown, true)
    assertEquals(state3.mouse.isLeftDown, true)
    assertEquals(state4.mouse.isLeftDown, true)
    assertEquals(state5.mouse.isLeftDown, true)
    assertEquals(state6.mouse.isLeftDown, false)
    assertEquals(state7.mouse.isLeftDown, false)
  }

  test("Mouse state.rigthMouseIsDown") {
    import MouseButton._

    val state2 =
      InputState.calculateNext(state, Batch(PointerEvent.Down(0, 0, RightMouseButton)), gamepadState1) // true
    val state3 = InputState.calculateNext(state2, Batch.empty, gamepadState1) // still true
    val state4 = InputState.calculateNext( // still true
      state3,
      Batch(PointerEvent.Down(20, 20, RightMouseButton)),
      gamepadState1
    )
    val state5 = InputState.calculateNext( // Still true
      state4,
      Batch(PointerEvent.Up(20, 20, RightMouseButton), PointerEvent.Down(20, 20, RightMouseButton)),
      gamepadState1
    )
    val state6 = // false
      InputState.calculateNext(state5, Batch(PointerEvent.Up(20, 20, RightMouseButton)), gamepadState1)
    val state7 = InputState.calculateNext( // Still false
      state6,
      Batch(PointerEvent.Down(20, 20, RightMouseButton), PointerEvent.Up(20, 20, RightMouseButton)),
      gamepadState1
    )

    assertEquals(state.mouse.isButtonDown(RightMouseButton), false)
    assertEquals(state2.mouse.isButtonDown(RightMouseButton), true)
    assertEquals(state3.mouse.isButtonDown(RightMouseButton), true)
    assertEquals(state4.mouse.isButtonDown(RightMouseButton), true)
    assertEquals(state5.mouse.isButtonDown(RightMouseButton), true)
    assertEquals(state6.mouse.isButtonDown(RightMouseButton), false)
    assertEquals(state7.mouse.isButtonDown(RightMouseButton), false)
  }

  test("Mouse state.scrolled") {
    val initialState = InputState.calculateNext(InputState.default, Batch(MouseEvent.Wheel(0, 0, -5)), gamepadState1)
    val state2 = InputState.calculateNext(
      initialState,
      Batch(MouseEvent.Wheel(0, 0, -5), MouseEvent.Wheel(0, 0, 10)),
      gamepadState1
    )
    val state3 = InputState.calculateNext(state2, Batch.empty[PointerEvent], gamepadState1)
    val state4 =
      InputState.calculateNext(
        state3,
        Batch(MouseEvent.Wheel(0, 0, -10), MouseEvent.Wheel(0, 0, 10)),
        gamepadState1
      )

    assertEquals(initialState.mouse.scrolled, Some(MouseWheel.ScrollUp))
    assertEquals(state2.mouse.scrolled, Some(MouseWheel.ScrollDown))
    assertEquals(state3.mouse.scrolled, Option.empty[MouseWheel])
    assertEquals(state4.mouse.scrolled, Option.empty[MouseWheel])
  }

  val events2: Batch[KeyboardEvent] =
    Batch(
      KeyboardEvent.KeyDown(Key.KEY_A),
      KeyboardEvent.KeyDown(Key.KEY_B),
      KeyboardEvent.KeyDown(Key.KEY_C),
      KeyboardEvent.KeyDown(Key.KEY_D),
      KeyboardEvent.KeyDown(Key.KEY_E),
      KeyboardEvent.KeyDown(Key.KEY_F),
      KeyboardEvent.KeyUp(Key.KEY_A),
      KeyboardEvent.KeyUp(Key.KEY_B),
      KeyboardEvent.KeyUp(Key.KEY_C)
    )

  test("Keyboard state.keysDown") {
    val state = InputState.calculateNext(inputState, events2, gamepadState1)

    val expected =
      Batch(
        Key.KEY_D,
        Key.KEY_E,
        Key.KEY_F
      )

    val actual =
      state.keyboard.keysDown

    assertEquals(actual, expected)
  }

  test("Keyboard state.keysAreDown") {
    val state = InputState.calculateNext(inputState, events2, gamepadState1)

    assertEquals(state.keyboard.keysAreDown(Key.KEY_D, Key.KEY_E, Key.KEY_F), true)
    assertEquals(state.keyboard.keysAreDown(Key.KEY_F, Key.KEY_D), true)
    assertEquals(state.keyboard.keysAreDown(Key.KEY_A), false)
    assertEquals(state.keyboard.keysAreDown(Key.KEY_D), true)
    assertEquals(state.keyboard.keysAreDown(Key.KEY_A, Key.KEY_D), false)
  }

  test("Keyboard state.keysAreUp") {
    val state = InputState.calculateNext(inputState, events2, gamepadState1)

    assertEquals(state.keyboard.keysAreUp(Key.KEY_A, Key.KEY_B, Key.KEY_C), true)
    assertEquals(state.keyboard.keysAreUp(Key.KEY_C, Key.KEY_B), true)
    assertEquals(state.keyboard.keysAreUp(Key.KEY_D), false)
    assertEquals(state.keyboard.keysAreUp(Key.KEY_A), true)
    assertEquals(state.keyboard.keysAreUp(Key.KEY_A, Key.KEY_D), false)
  }

  test("Keyboard state.keysReleased") {
    val state = InputState.calculateNext(inputState, events2, gamepadState1)

    val expected =
      Batch(
        Key.KEY_A,
        Key.KEY_B,
        Key.KEY_C
      )

    val actual =
      state.keyboard.keysReleased

    assertEquals(actual, expected)

  }

  test("Keyboard state.keysDown persist across frames") {
    val state1 = InputState.calculateNext(inputState, events2, gamepadState1)

    assertEquals(state1.keyboard.keysDown, Batch(Key.KEY_D, Key.KEY_E, Key.KEY_F))

    val state2 = InputState.calculateNext(
      state1,
      Batch(
        KeyboardEvent.KeyDown(Key.KEY_Z),
        KeyboardEvent.KeyUp(Key.KEY_D)
      ),
      gamepadState1
    )

    assertEquals(state2.keyboard.keysDown, Batch(Key.KEY_E, Key.KEY_F, Key.KEY_Z))
  }

  test("Keyboard state.lastKeyHeldDown") {

    assertEquals(inputState.keyboard.lastKeyHeldDown, None)

    val state1 =
      InputState.calculateNext(
        InputState.calculateNext(inputState, events2, gamepadState1),
        Batch(KeyboardEvent.KeyDown(Key.KEY_E), KeyboardEvent.KeyDown(Key.KEY_F)),
        gamepadState1
      )

    assertEquals(state1.keyboard.lastKeyHeldDown, Some(Key.KEY_F))

    val state2 =
      InputState.calculateNext(
        state1,
        Batch(KeyboardEvent.KeyDown(Key.KEY_E)),
        gamepadState1
      )

    assertEquals(state2.keyboard.lastKeyHeldDown, Some(Key.KEY_E))

    val state3 =
      InputState.calculateNext(
        state2,
        Batch(
          KeyboardEvent.KeyUp(Key.KEY_D),
          KeyboardEvent.KeyUp(Key.KEY_E),
          KeyboardEvent.KeyUp(Key.KEY_F)
        ),
        gamepadState1
      )

    assertEquals(state3.keyboard.lastKeyHeldDown, None)
  }

  val events3: Batch[InputEvent] =
    Batch(
      KeyboardEvent.KeyDown(Key.KEY_A),
      KeyboardEvent.KeyDown(Key.KEY_B),
      KeyboardEvent.KeyDown(Key.KEY_C),
      KeyboardEvent.KeyDown(Key.KEY_D),
      PointerEvent.Move(10, 10),
      PointerEvent.Down(10, 10),
      MouseEvent.Wheel(10, 10, -15)
    )

  val gamepadState2: Gamepad =
    new Gamepad(
      true,
      new GamepadAnalogControls(
        new AnalogAxis(-1.0, -1.0, false),
        new AnalogAxis(0.5, 0.0, true),
        2
      ),
      new GamepadDPad(true, false, true, false),
      new GamepadButtons(
        true, false, false, false, false, false, false, false, false, false, false, false
      )
    )

  test("Mapping combinations of inputs.keyboard combo found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int](Combo.KeyInputs(Key.KEY_C, Key.KEY_A, Key.KEY_B) -> 10)

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      10

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.keyboard combo not found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int]()
        .add(
          Combo.KeyInputs(Key.ARROW_UP) -> 10
        )

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      0

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.mouse combo found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int](Combo.MouseInputs(MouseInput.MouseDown, MouseInput.MouseAt(10, 10)) -> 10)

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      10

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.mouse combo not found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int]()
        .add(
          Combo.MouseInputs(MouseInput.MouseUp) -> 10
        )

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      0

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.gamepad combo found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int](
        Combo.GamepadInputs(GamepadInput.Cross, GamepadInput.LEFT_ANALOG(_ < -0.5, _ => true, false)) -> 10
      )

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      10

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.gamepad combo not found") {

    val mappings: InputMapping[Int] =
      InputMapping[Int]()
        .add(
          Combo.GamepadInputs(GamepadInput.Triangle) -> 10
        )

    val state = InputState.calculateNext(inputState, events3, gamepadState2)

    val expected =
      0

    val actual =
      state.mapInputs(mappings, 0)

    assertEquals(actual, expected)
  }

  test("Mapping combinations of inputs.Mixed combo") {

    val comboA =
      Combo
        .withGamepadInputs(
          GamepadInput.Cross,
          GamepadInput.RIGHT_ANALOG(_ > 0.4, _ == 0.0, true)
        )
        .withMouseInputs(MouseInput.MouseDown, MouseInput.MouseWheelUp)
        .withKeyInputs(Key.KEY_A, Key.KEY_B)

    val comboB =
      Combo
        .withKeyInputs(Key.ARROW_UP, Key.ARROW_RIGHT)

    val mappings: InputMapping[String] =
      InputMapping[String](comboA -> "Combo A met", comboB -> "Combo B met")

    val mappingResult1 =
      InputState
        .calculateNext(inputState, events3, gamepadState2)
        .mapInputs(mappings, "Combo not met! (1)")

    val mappingResult2 =
      InputState
        .calculateNext(
          inputState,
          Batch(KeyboardEvent.KeyDown(Key.ARROW_UP), KeyboardEvent.KeyDown(Key.ARROW_RIGHT)),
          gamepadState2
        )
        .mapInputs(mappings, "Combo not met! (2)")

    val mappingResult3 =
      InputState
        .calculateNext(inputState, Batch(KeyboardEvent.KeyDown(Key.ARROW_LEFT)), gamepadState2)
        .mapInputs(mappings, "Combo not met! (3)")

    assertEquals(mappingResult1, "Combo A met")
    assertEquals(mappingResult2, "Combo B met")
    assertEquals(mappingResult3, "Combo not met! (3)")
  }

}
