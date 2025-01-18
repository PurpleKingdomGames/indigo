package indigo.shared.events

import indigo.shared.collections.Batch
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.input.Pointers

/** Holds a snapshot of the states of the various input types as they were entering this frame.
  *
  * @param mouse
  *   Current state of the mouse
  * @param keyboard
  *   Current state of the keyboard
  * @param gamepad
  *   Current state of the gamepad
  */
final class InputState(val mouse: Mouse, val keyboard: Keyboard, val gamepad: Gamepad, val pointers: Pointers) {

  /** Given some input mappings, produce a guaranteed value A based on the current InputState.
    */
  def mapInputs[A](mappings: InputMapping[A], default: A): A =
    mappings.find(mouse, keyboard, gamepad).getOrElse(default)

  /** Given some input mappings, produce an optional value A based on the current InputState.
    */
  def mapInputsOption[A](mappings: InputMapping[A]): Option[A] =
    mappings.find(mouse, keyboard, gamepad)

}

object InputState {
  val default: InputState =
    InputState(Mouse.default, Keyboard.default, Gamepad.default, Pointers.default)

  def calculateNext(
      previous: InputState,
      events: Batch[InputEvent],
      gamepadState: Gamepad
  ): InputState =
    val pointers = Pointers.calculateNext(previous.pointers, events.collect { case e: PointerEvent => e });

    InputState(
      Mouse(pointers, events.collect { case e: MouseEvent.Wheel => e }),
      Keyboard.calculateNext(previous.keyboard, events.collect { case e: KeyboardEvent => e }),
      gamepadState,
      pointers
    )
}
