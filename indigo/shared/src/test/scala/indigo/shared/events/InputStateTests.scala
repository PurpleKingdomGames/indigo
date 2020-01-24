package indigo.shared.events

import utest._
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.constants.Keys
import indigo.shared.constants.Key

object InputStateTests extends TestSuite {

  val tests: Tests =
    Tests {

      val bounds: Rectangle =
        Rectangle(10, 10, 100, 100)

      val inputState: InputState =
        InputState.default

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

        val state = InputState.calculateNext(inputState, events)

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

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0))).mouse.mouseClicked ==> false
        }

        "mouseClickAt" - {
          state.mouse.mouseClickAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0))).mouse.mouseClickAt ==> None
        }

        "mouseUpAt" - {
          state.mouse.mouseUpAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseDown(0, 0))).mouse.mouseUpAt ==> None
        }

        "mouseDownAt" - {
          state.mouse.mouseDownAt ==> Some(Point(10, 10))

          InputState.calculateNext(inputState, List(MouseEvent.MouseUp(0, 0))).mouse.mouseDownAt ==> None
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

          val state2 = InputState.calculateNext(state, List(MouseEvent.MouseDown(0, 0)))                                // true
          val state3 = InputState.calculateNext(state2, Nil)                                                            // still true
          val state4 = InputState.calculateNext(state3, List(MouseEvent.MouseDown(20, 20)))                             // still true
          val state5 = InputState.calculateNext(state4, List(MouseEvent.MouseUp(20, 20), MouseEvent.MouseDown(20, 20))) // Still true
          val state6 = InputState.calculateNext(state5, List(MouseEvent.MouseUp(20, 20)))                               // false
          val state7 = InputState.calculateNext(state6, List(MouseEvent.MouseDown(20, 20), MouseEvent.MouseUp(20, 20))) // Still false

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
            KeyboardEvent.KeyDown(Keys.KEY_A),
            KeyboardEvent.KeyDown(Keys.KEY_B),
            KeyboardEvent.KeyDown(Keys.KEY_C),
            KeyboardEvent.KeyDown(Keys.KEY_D),
            KeyboardEvent.KeyDown(Keys.KEY_E),
            KeyboardEvent.KeyDown(Keys.KEY_F),
            KeyboardEvent.KeyUp(Keys.KEY_A),
            KeyboardEvent.KeyUp(Keys.KEY_B),
            KeyboardEvent.KeyUp(Keys.KEY_C)
          )

        "keysDown" - {
          val state = InputState.calculateNext(inputState, events)

          val expected =
            List(
              Keys.KEY_D,
              Keys.KEY_E,
              Keys.KEY_F
            )

          val actual =
            state.keyboard.keysDown

          actual ==> expected
        }

        "keysAreDown" - {
          val state = InputState.calculateNext(inputState, events)

          state.keyboard.keysAreDown(Keys.KEY_D, Keys.KEY_E, Keys.KEY_F) ==> true
          state.keyboard.keysAreDown(Keys.KEY_F, Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A) ==> false
          state.keyboard.keysAreDown(Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "keysAreUp" - {
          val state = InputState.calculateNext(inputState, events)

          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_B, Keys.KEY_C) ==> true
          state.keyboard.keysAreUp(Keys.KEY_C, Keys.KEY_B) ==> true
          state.keyboard.keysAreUp(Keys.KEY_D) ==> false
          state.keyboard.keysAreUp(Keys.KEY_A) ==> true
          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "keysReleased" - {
          val state = InputState.calculateNext(inputState, events)

          val expected =
            List(
              Keys.KEY_A,
              Keys.KEY_B,
              Keys.KEY_C
            )

          val actual =
            state.keyboard.keysReleased

          actual ==> expected

        }

        "keysDown persist across frames" - {
          val state1 = InputState.calculateNext(inputState, events)

          state1.keyboard.keysDown ==> List(Keys.KEY_D, Keys.KEY_E, Keys.KEY_F)

          val state2 = InputState.calculateNext(
            state1,
            List(
              KeyboardEvent.KeyDown(Keys.KEY_Z),
              KeyboardEvent.KeyUp(Keys.KEY_D)
            )
          )

          state2.keyboard.keysDown ==> List(Keys.KEY_E, Keys.KEY_F, Keys.KEY_Z)
        }

        "lastKeyHeldDown" - {

          inputState.keyboard.lastKeyHeldDown ==> None

          val state1 =
            InputState.calculateNext(
              InputState.calculateNext(inputState, events),
              List(KeyboardEvent.KeyDown(Keys.KEY_E), KeyboardEvent.KeyDown(Keys.KEY_F))
            )

          state1.keyboard.lastKeyHeldDown ==> Some(Keys.KEY_F)

          val state2 =
            InputState.calculateNext(
              state1,
              List(KeyboardEvent.KeyDown(Keys.KEY_E))
            )

          state2.keyboard.lastKeyHeldDown ==> Some(Keys.KEY_E)

          val state3 =
            InputState.calculateNext(
              state2,
              List(
                KeyboardEvent.KeyUp(Keys.KEY_D),
                KeyboardEvent.KeyUp(Keys.KEY_E),
                KeyboardEvent.KeyUp(Keys.KEY_F)
              )
            )

          state3.keyboard.lastKeyHeldDown ==> None
        }

      }

    }

}
