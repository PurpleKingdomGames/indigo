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

        val state = inputState.calculateNext(events)

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

          inputState.calculateNext(List(MouseEvent.MouseDown(0, 0))).mouse.mouseClicked ==> false
        }

        "mouseClickAt" - {
          state.mouse.mouseClickAt ==> Some(Point(10, 10))

          inputState.calculateNext(List(MouseEvent.MouseDown(0, 0))).mouse.mouseClickAt ==> None
        }

        "mouseUpAt" - {
          state.mouse.mouseUpAt ==> Some(Point(10, 10))
          inputState.calculateNext(List(MouseEvent.MouseDown(0, 0))).mouse.mouseUpAt ==> None
        }

        "mouseDownAt" - {
          state.mouse.mouseDownAt ==> Some(Point(10, 10))
          inputState.calculateNext(List(MouseEvent.MouseUp(0, 0))).mouse.mouseDownAt ==> None
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

        "wasMousePositionAt" - { 1 ==> 2 }

        "wasMouseClickedWithin" - { 1 ==> 2 }

        "wasMouseUpWithin" - { 1 ==> 2 }

        "wasMouseDownWithin" - { 1 ==> 2 }

        "wasMousePositionWithin" - { 1 ==> 2 }

        "leftMouseIsDown" - { 1 ==> 2 }
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
          val state = inputState.calculateNext(events)

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
          val state = inputState.calculateNext(events)

          state.keyboard.keysAreDown(Keys.KEY_D, Keys.KEY_E, Keys.KEY_F) ==> true
          state.keyboard.keysAreDown(Keys.KEY_F, Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A) ==> false
          state.keyboard.keysAreDown(Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "keysAreUp" - {
          val state = inputState.calculateNext(events)

          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_B, Keys.KEY_C) ==> true
          state.keyboard.keysAreUp(Keys.KEY_C, Keys.KEY_B) ==> true
          state.keyboard.keysAreUp(Keys.KEY_D) ==> false
          state.keyboard.keysAreUp(Keys.KEY_A) ==> true
          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "keysReleased" - {
          val state = inputState.calculateNext(events)

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
          val state1 = inputState.calculateNext(events)

          state1.keyboard.keysDown ==> List(Keys.KEY_D, Keys.KEY_E, Keys.KEY_F)

          val state2 = state1.calculateNext(
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
            inputState
              .calculateNext(events)
              .calculateNext(List(KeyboardEvent.KeyDown(Keys.KEY_E), KeyboardEvent.KeyDown(Keys.KEY_F)))

          state1.keyboard.lastKeyHeldDown ==> Some(Keys.KEY_F)

          val state2 =
            state1
              .calculateNext(List(KeyboardEvent.KeyDown(Keys.KEY_E)))

          state2.keyboard.lastKeyHeldDown ==> Some(Keys.KEY_E)

          val state3 =
            state2
              .calculateNext(
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
