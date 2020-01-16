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

        "position" - { 1 ==> 2 }
        "leftMouseIsDown" - { 1 ==> 2 }
        "mousePressed" - { 1 ==> 2 }
        "mouseReleased" - { 1 ==> 2 }
        "mouseClicked" - { 1 ==> 2 }
        "mouseClickAt" - { 1 ==> 2 }
        "mouseUpAt" - { 1 ==> 2 }
        "mouseDownAt" - { 1 ==> 2 }
        "wasMouseClickedAt" - { 1 ==> 2 }
        "wasMouseUpAt" - { 1 ==> 2 }
        "wasMouseDownAt" - { 1 ==> 2 }
        "wasMousePositionAt" - { 1 ==> 2 }
        "wasMouseClickedWithin" - { 1 ==> 2 }
        "wasMouseUpWithin" - { 1 ==> 2 }
        "wasMouseDownWithin" - { 1 ==> 2 }
        "wasMousePositionWithin" - { 1 ==> 2 }

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

        "keys up" - {
          val state = inputState.calculateNext(events)

          val expected =
            List(
              Keys.KEY_A,
              Keys.KEY_B,
              Keys.KEY_C,
              Keys.KEY_Z
            )

          val actual =
            state.keyboard.keysUp

          actual ==> expected
        }

        "keys down" - {
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

        "keys are down" - {
          val state = inputState.calculateNext(events)

          state.keyboard.keysAreDown(Keys.KEY_D, Keys.KEY_E, Keys.KEY_F) ==> true
          state.keyboard.keysAreDown(Keys.KEY_F, Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A) ==> false
          state.keyboard.keysAreDown(Keys.KEY_D) ==> true
          state.keyboard.keysAreDown(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "keys are up" - {
          val state = inputState.calculateNext(events)

          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_B, Keys.KEY_C) ==> true
          state.keyboard.keysAreUp(Keys.KEY_C, Keys.KEY_B) ==> true
          state.keyboard.keysAreUp(Keys.KEY_D) ==> false
          state.keyboard.keysAreUp(Keys.KEY_A) ==> true
          state.keyboard.keysAreUp(Keys.KEY_A, Keys.KEY_D) ==> false
        }

        "last key held down" - {

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
