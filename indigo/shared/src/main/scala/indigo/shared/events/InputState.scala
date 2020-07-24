package indigo.shared.events

import indigo.shared.input.Gamepad
import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard

final class InputState(val mouse: Mouse, val keyboard: Keyboard, val gamepad: Gamepad) {

  def mapInputs[A](mappings: InputMapping[A], default: A): A =
    mappings.find(mouse, keyboard, gamepad).getOrElse(default)

  def mapInputsOption[A](mappings: InputMapping[A]): Option[A] =
    mappings.find(mouse, keyboard, gamepad)

}

object InputState {
  val default: InputState =
    new InputState(Mouse.default, Keyboard.default, Gamepad.default)

  def calculateNext(previous: InputState, events: List[InputEvent], gamepadState: Gamepad): InputState =
    new InputState(
      Mouse.calculateNext(previous.mouse, events.collect { case e: MouseEvent => e }),
      Keyboard.calculateNext(previous.keyboard, events.collect { case e: KeyboardEvent => e }),
      gamepadState
    )
}
