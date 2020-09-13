package indigo.shared.events

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.constants.Key
import indigo.shared.input.Gamepad
import indigo.shared.input.GamepadAnalogControls
import indigo.shared.input.AnalogAxis
import indigo.shared.input.GamepadDPad
import indigo.shared.input.GamepadButtons

object InputStateTests extends TestSuite {

  val tests: Tests =
    Tests {

      val bounds: Rectangle =
        Rectangle(10, 10, 100, 100)

      val inputState: InputState =
        InputState.default

      val gamepadState: Gamepad =
        Gamepad.default

      "The default state object does the expected thing" - {
        inputState.mouse.leftMouseIsDown ==> false
        inputState.mouse.position ==> Point.zero

        inputState.mouse.wasMouseClickedWithin(bounds) ==> false
      }

      "Mouse state" - {

        val events: List[MouseEvent] =
          List(
            MouseEvent.Move(10, 10),
            MouseEvent.MouseDown(10, 10),
            MouseEvent.MouseUp(10, 10),
            MouseEvent.Click(10, 10)
          )

        val state = InputState.calculateNext(inputState, events, gamepadState)

        "position" - {
          state.mouse.position === Point(10, 10) ==> true
        }

        "mousePressed" - {
          state.mouse.mousePressed ==> true
        }

        "mouseReleased" - {
          state.mouse.mouseReleased ==> true
        }

        "mouseClicked" - {
          state.mouse.mouseClicked ==> true

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState).mouse.mouseClicked ==> false
        }

        "mouseClickAt" - {
          state.mouse.mouseClickAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState).mouse.mouseClickAt ==> None
        }

        "mouseUpAt" - {
          state.mouse.mouseUpAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0)), gamepadState).mouse.mouseUpAt ==> None
        }

        "mouseDownAt" - {
          state.mouse.mouseDownAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseUp(0, 0)), gamepadState).mouse.mouseDownAt ==> None
        }

        "wasMouseClickedAt" - {
          state.mouse.wasMouseClickedAt(10, 10) ==> true
          state.mouse.wasMouseClickedAt(20, 10) ==> false
        }

        "wasMouseUpAt" - {
          state.mouse.wasMouseUpAt(10, 10) ==> true
          state.mouse.wasMouseUpAt(20, 10) ==> false
        }

        "wasMouseDownAt" - {
          state.mouse.wasMouseDownAt(10, 10) ==> true
          state.mouse.wasMouseDownAt(20, 10) ==> false
        }

        "wasMousePositionAt" - {
          state.mouse.wasMousePositionAt(Point.zero) ==> false
          state.mouse.wasMousePositionAt(Point(10, 10)) ==> true
        }

        "wasMouseClickedWithin" - {
          state.mouse.wasMouseClickedWithin(Rectangle(0, 0, 5, 5)) ==> false
          state.mouse.wasMouseClickedWithin(Rectangle(50, 50, 5, 5)) ==> false
          state.mouse.wasMouseClickedWithin(Rectangle(5, 5, 10, 10)) ==> true
        }

        "wasMouseUpWithin" - {
          state.mouse.wasMouseUpWithin(Rectangle(0, 0, 5, 5)) ==> false
          state.mouse.wasMouseUpWithin(Rectangle(50, 50, 5, 5)) ==> false
          state.mouse.wasMouseUpWithin(Rectangle(5, 5, 10, 10)) ==> true
        }

        "wasMouseDownWithin" - {
          state.mouse.wasMouseDownWithin(Rectangle(0, 0, 5, 5)) ==> false
          state.mouse.wasMouseDownWithin(Rectangle(50, 50, 5, 5)) ==> false
          state.mouse.wasMouseDownWithin(Rectangle(5, 5, 10, 10)) ==> true
        }

        "wasMousePositionWithin" - {
          state.mouse.wasMousePositionWithin(Rectangle(0, 0, 5, 5)) ==> false
          state.mouse.wasMousePositionWithin(Rectangle(50, 50, 5, 5)) ==> false
          state.mouse.wasMousePositionWithin(Rectangle(5, 5, 10, 10)) ==> true
        }

        "leftMouseIsDown" - {

          val state2 = InputState.calculateNext(state, List(MouseEvent.MouseDown(0, 0)), gamepadState)                                // true
          val state3 = InputState.calculateNext(state2, Nil, gamepadState)                                                            // still true
          val state4 = InputState.calculateNext(state3, List(MouseEvent.MouseDown(20, 20)), gamepadState)                             // still true
          val state5 = InputState.calculateNext(state4, List(MouseEvent.MouseUp(20, 20), MouseEvent.MouseDown(20, 20)), gamepadState) // Still true
          val state6 = InputState.calculateNext(state5, List(MouseEvent.MouseUp(20, 20)), gamepadState)                               // false
          val state7 = InputState.calculateNext(state6, List(MouseEvent.MouseDown(20, 20), MouseEvent.MouseUp(20, 20)), gamepadState) // Still false

          state.mouse.leftMouseIsDown ==> false
          state2.mouse.leftMouseIsDown ==> true
          state3.mouse.leftMouseIsDown ==> true
          state4.mouse.leftMouseIsDown ==> true
          state5.mouse.leftMouseIsDown ==> true
          state6.mouse.leftMouseIsDown ==> false
          state7.mouse.leftMouseIsDown ==> false
        }
      }

      "Keyboard state" - {

        val events: List[KeyboardEvent] =
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

        "keysDown" - {
          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            List(
              Key.KEY_D,
              Key.KEY_E,
              Key.KEY_F
            )

          val actual =
            state.keyboard.keysDown

          actual ==> expected
        }

        "keysAreDown" - {
          val state = InputState.calculateNext(inputState, events, gamepadState)

          state.keyboard.keysAreDown(Key.KEY_D, Key.KEY_E, Key.KEY_F) ==> true
          state.keyboard.keysAreDown(Key.KEY_F, Key.KEY_D) ==> true
          state.keyboard.keysAreDown(Key.KEY_A) ==> false
          state.keyboard.keysAreDown(Key.KEY_D) ==> true
          state.keyboard.keysAreDown(Key.KEY_A, Key.KEY_D) ==> false
        }

        "keysAreUp" - {
          val state = InputState.calculateNext(inputState, events, gamepadState)

          state.keyboard.keysAreUp(Key.KEY_A, Key.KEY_B, Key.KEY_C) ==> true
          state.keyboard.keysAreUp(Key.KEY_C, Key.KEY_B) ==> true
          state.keyboard.keysAreUp(Key.KEY_D) ==> false
          state.keyboard.keysAreUp(Key.KEY_A) ==> true
          state.keyboard.keysAreUp(Key.KEY_A, Key.KEY_D) ==> false
        }

        "keysReleased" - {
          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            List(
              Key.KEY_A,
              Key.KEY_B,
              Key.KEY_C
            )

          val actual =
            state.keyboard.keysReleased

          actual ==> expected

        }

        "keysDown persist across frames" - {
          val state1 = InputState.calculateNext(inputState, events, gamepadState)

          state1.keyboard.keysDown ==> List(Key.KEY_D, Key.KEY_E, Key.KEY_F)

          val state2 = InputState.calculateNext(
            state1,
            List(
              KeyboardEvent.KeyDown(Key.KEY_Z),
              KeyboardEvent.KeyUp(Key.KEY_D)
            ),
            gamepadState
          )

          state2.keyboard.keysDown ==> List(Key.KEY_E, Key.KEY_F, Key.KEY_Z)
        }

        "lastKeyHeldDown" - {

          inputState.keyboard.lastKeyHeldDown ==> None

          val state1 =
            InputState.calculateNext(
              InputState.calculateNext(inputState, events, gamepadState),
              List(KeyboardEvent.KeyDown(Key.KEY_E), KeyboardEvent.KeyDown(Key.KEY_F)),
              gamepadState
            )

          state1.keyboard.lastKeyHeldDown ==> Some(Key.KEY_F)

          val state2 =
            InputState.calculateNext(
              state1,
              List(KeyboardEvent.KeyDown(Key.KEY_E)),
              gamepadState
            )

          state2.keyboard.lastKeyHeldDown ==> Some(Key.KEY_E)

          val state3 =
            InputState.calculateNext(
              state2,
              List(
                KeyboardEvent.KeyUp(Key.KEY_D),
                KeyboardEvent.KeyUp(Key.KEY_E),
                KeyboardEvent.KeyUp(Key.KEY_F)
              ),
              gamepadState
            )

          state3.keyboard.lastKeyHeldDown ==> None
        }

      }

      "Mapping combinations of inputs" - {

        val events: List[InputEvent] =
          List(
            KeyboardEvent.KeyDown(Key.KEY_A),
            KeyboardEvent.KeyDown(Key.KEY_B),
            KeyboardEvent.KeyDown(Key.KEY_C),
            KeyboardEvent.KeyDown(Key.KEY_D),
            MouseEvent.Move(10, 10),
            MouseEvent.MouseDown(10, 10)
          )

        val gamepadState: Gamepad =
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

        "keyboard combo found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int](Combo.KeyInputs(Key.KEY_C, Key.KEY_A, Key.KEY_B) -> 10)

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            10

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "keyboard combo not found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int]
              .add(
                Combo.KeyInputs(Key.UP_ARROW) -> 10
              )

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            0

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "mouse combo found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int](Combo.MouseInputs(MouseInput.MouseDown, MouseInput.MouseAt(10, 10)) -> 10)

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            10

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "mouse combo not found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int]
              .add(
                Combo.MouseInputs(MouseInput.MouseUp) -> 10
              )

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            0

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "gamepad combo found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int](
              Combo.GamepadInputs(GamepadInput.Cross, GamepadInput.LEFT_ANALOG(_ < -0.5, _ => true, false)) -> 10
            )

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            10

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "gamepad combo not found" - {

          val mappings: InputMapping[Int] =
            InputMapping[Int]
              .add(
                Combo.GamepadInputs(GamepadInput.Triangle) -> 10
              )

          val state = InputState.calculateNext(inputState, events, gamepadState)

          val expected =
            0

          val actual =
            state.mapInputs(mappings, 0)

          actual ==> expected
        }

        "Mixed combo" - {

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
              .calculateNext(inputState, events, gamepadState)
              .mapInputs(mappings, "Combo not met! (1)")

          val mappingResult2 =
            InputState
              .calculateNext(inputState, List(KeyboardEvent.KeyDown(Key.UP_ARROW), KeyboardEvent.KeyDown(Key.RIGHT_ARROW)), gamepadState)
              .mapInputs(mappings, "Combo not met! (2)")

          val mappingResult3 =
            InputState
              .calculateNext(inputState, List(KeyboardEvent.KeyDown(Key.LEFT_ARROW)), gamepadState)
              .mapInputs(mappings, "Combo not met! (3)")

          mappingResult1 ==> "Combo A met"
          mappingResult2 ==> "Combo B met"
          mappingResult3 ==> "Combo not met! (3)"
        }

      }

    }

}
