package indigo.shared.events

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.constants.Key
import indigo.shared.input.Gamepad
import indigo.shared.input.GamepadAnalogControls
import indigo.shared.input.AnalogAxis
import indigo.shared.input.GamepadDPad
import indigo.shared.input.GamepadButtons

class InputStateTests extends munit.FunSuite {

  val bounds: Rectangle =
    Rectangle(10, 10, 100, 100)

  val inputState: InputState =
    InputState.default

  val gamepadState1: Gamepad =
    Gamepad.default

  test("The default state object does the expected thing") {
    assertEquals(inputState.mouse.leftMouseIsDown, false)
    assertEquals(inputState.mouse.position, Point.zero)

    assertEquals(inputState.mouse.wasMouseClickedWithin(bounds), false)
  }

  val events1: List[MouseEvent] =
    List(
      MouseEvent.Move(10, 10),
      MouseEvent.MouseDown(10, 10),
      MouseEvent.MouseUp(10, 10),
      MouseEvent.Click(10, 10)
    )

  val state = InputState.calculateNext(inputState, events1, gamepadState1)

  test("Mouse state.position") {
    assertEquals(state.mouse.position === Point(10, 10), true)
  }

  test("Mouse state.mousePressed") {
    assertEquals(state.mouse.mousePressed, true)
  }

  test("Mouse state.mouseReleased") {
    assertEquals(state.mouse.mouseReleased, true)
  }

  test("Mouse state.mouseClicked") {
    assertEquals(state.mouse.mouseClicked, true)

    assertEquals(InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState1).mouse.mouseClicked, false)
  }

  test("Mouse state.mouseClickAt") {
    assertEquals(state.mouse.mouseClickAt, Some(Point(10, 10)))

    assertEquals(InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState1).mouse.mouseClickAt, None)
  }

  test("Mouse state.mouseUpAt") {
    assertEquals(state.mouse.mouseUpAt, Some(Point(10, 10)))

    assertEquals(InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState1).mouse.mouseUpAt, None)
  }

  test("Mouse state.mouseDownAt") {
    assertEquals(state.mouse.mouseDownAt, Some(Point(10, 10)))

    assertEquals(InputState.calculateNext(inputState, List(MouseEvent.MouseUp(0, 0)), gamepadState1).mouse.mouseDownAt, None)
  }

  test("Mouse state.wasMouseClickedAt") {
    assertEquals(state.mouse.wasMouseClickedAt(10, 10), true)
    assertEquals(state.mouse.wasMouseClickedAt(20, 10), false)
  }

  test("Mouse state.wasMouseUpAt") {
    assertEquals(state.mouse.wasMouseUpAt(10, 10), true)
    assertEquals(state.mouse.wasMouseUpAt(20, 10), false)
  }

  test("Mouse state.wasMouseDownAt") {
    assertEquals(state.mouse.wasMouseDownAt(10, 10), true)
    assertEquals(state.mouse.wasMouseDownAt(20, 10), false)
  }

  test("Mouse state.wasMousePositionAt") {
    assertEquals(state.mouse.wasMousePositionAt(Point.zero), false)
    assertEquals(state.mouse.wasMousePositionAt(Point(10, 10)), true)
  }

  test("Mouse state.wasMouseClickedWithin") {
    assertEquals(state.mouse.wasMouseClickedWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasMouseClickedWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasMouseClickedWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.wasMouseUpWithin") {
    assertEquals(state.mouse.wasMouseUpWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasMouseUpWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasMouseUpWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.wasMouseDownWithin") {
    assertEquals(state.mouse.wasMouseDownWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasMouseDownWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasMouseDownWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.wasMousePositionWithin") {
    assertEquals(state.mouse.wasMousePositionWithin(Rectangle(0, 0, 5, 5)), false)
    assertEquals(state.mouse.wasMousePositionWithin(Rectangle(50, 50, 5, 5)), false)
    assertEquals(state.mouse.wasMousePositionWithin(Rectangle(5, 5, 10, 10)), true)
  }

  test("Mouse state.leftMouseIsDown") {

    val state2 = InputState.calculateNext(state, List(MouseEvent.MouseDown(0, 0)), gamepadState1)                                // true
    val state3 = InputState.calculateNext(state2, Nil, gamepadState1)                                                            // still true
    val state4 = InputState.calculateNext(state3, List(MouseEvent.MouseDown(20, 20)), gamepadState1)                             // still true
    val state5 = InputState.calculateNext(state4, List(MouseEvent.MouseUp(20, 20), MouseEvent.MouseDown(20, 20)), gamepadState1) // Still true
    val state6 = InputState.calculateNext(state5, List(MouseEvent.MouseUp(20, 20)), gamepadState1)                               // false
    val state7 = InputState.calculateNext(state6, List(MouseEvent.MouseDown(20, 20), MouseEvent.MouseUp(20, 20)), gamepadState1) // Still false

    assertEquals(state.mouse.leftMouseIsDown, false)
    assertEquals(state2.mouse.leftMouseIsDown, true)
    assertEquals(state3.mouse.leftMouseIsDown, true)
    assertEquals(state4.mouse.leftMouseIsDown, true)
    assertEquals(state5.mouse.leftMouseIsDown, true)
    assertEquals(state6.mouse.leftMouseIsDown, false)
    assertEquals(state7.mouse.leftMouseIsDown, false)
  }

  val events2: List[KeyboardEvent] =
    List(
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
      List(
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
      List(
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

    assertEquals(state1.keyboard.keysDown, List(Key.KEY_D, Key.KEY_E, Key.KEY_F))

    val state2 = InputState.calculateNext(
      state1,
      List(
        KeyboardEvent.KeyDown(Key.KEY_Z),
        KeyboardEvent.KeyUp(Key.KEY_D)
      ),
      gamepadState1
    )

    assertEquals(state2.keyboard.keysDown, List(Key.KEY_E, Key.KEY_F, Key.KEY_Z))
  }

  test("Keyboard state.lastKeyHeldDown") {

    assertEquals(inputState.keyboard.lastKeyHeldDown, None)

    val state1 =
      InputState.calculateNext(
        InputState.calculateNext(inputState, events2, gamepadState1),
        List(KeyboardEvent.KeyDown(Key.KEY_E), KeyboardEvent.KeyDown(Key.KEY_F)),
        gamepadState1
      )

    assertEquals(state1.keyboard.lastKeyHeldDown, Some(Key.KEY_F))

    val state2 =
      InputState.calculateNext(
        state1,
        List(KeyboardEvent.KeyDown(Key.KEY_E)),
        gamepadState1
      )

    assertEquals(state2.keyboard.lastKeyHeldDown, Some(Key.KEY_E))

    val state3 =
      InputState.calculateNext(
        state2,
        List(
          KeyboardEvent.KeyUp(Key.KEY_D),
          KeyboardEvent.KeyUp(Key.KEY_E),
          KeyboardEvent.KeyUp(Key.KEY_F)
        ),
        gamepadState1
      )

    assertEquals(state3.keyboard.lastKeyHeldDown, None)
  }

  val events3: List[InputEvent] =
    List(
      KeyboardEvent.KeyDown(Key.KEY_A),
      KeyboardEvent.KeyDown(Key.KEY_B),
      KeyboardEvent.KeyDown(Key.KEY_C),
      KeyboardEvent.KeyDown(Key.KEY_D),
      MouseEvent.Move(10, 10),
      MouseEvent.MouseDown(10, 10)
    )

  val gamepadState2: Gamepad =
    new Gamepad(
      true,
      new GamepadAnalogControls(
        new AnalogAxis(-1.0, -1.0, false),
        new AnalogAxis(0.5, 0.0, true)
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
          Combo.KeyInputs(Key.UP_ARROW) -> 10
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
        .withMouseInputs(MouseInput.MouseDown)
        .withKeyInputs(Key.KEY_A, Key.KEY_B)

    val comboB =
      Combo
        .withKeyInputs(Key.UP_ARROW, Key.RIGHT_ARROW)

    val mappings: InputMapping[String] =
      InputMapping[String](comboA -> "Combo A met", comboB -> "Combo B met")

    val mappingResult1 =
      InputState
        .calculateNext(inputState, events3, gamepadState2)
        .mapInputs(mappings, "Combo not met! (1)")

    val mappingResult2 =
      InputState
        .calculateNext(inputState, List(KeyboardEvent.KeyDown(Key.UP_ARROW), KeyboardEvent.KeyDown(Key.RIGHT_ARROW)), gamepadState2)
        .mapInputs(mappings, "Combo not met! (2)")

    val mappingResult3 =
      InputState
        .calculateNext(inputState, List(KeyboardEvent.KeyDown(Key.LEFT_ARROW)), gamepadState2)
        .mapInputs(mappings, "Combo not met! (3)")

    assertEquals(mappingResult1, "Combo A met")
    assertEquals(mappingResult2, "Combo B met")
    assertEquals(mappingResult3, "Combo not met! (3)")
  }

}
