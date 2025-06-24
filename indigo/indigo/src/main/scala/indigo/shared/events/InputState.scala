package indigo.shared.events

import indigo.shared.collections.Batch
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.input.Pen
import indigo.shared.input.Pointers
import indigo.shared.input.Touch
import indigo.shared.input.Wheel

import scala.annotation.nowarn

/** Holds a snapshot of the states of the various input types as they were entering this frame.
  *
  * @param mouse
  *   Current state of any mice
  * @param keyboard
  *   Current state of the keyboard
  * @param pen
  *   Current state of any pen inputs
  * @param touch
  *   Current state of and touch inputs
  * @param gamepad
  *   Current state of the gamepad
  * @param pointers
  *   Current state of all pointers, including mouse, pen and touch
  */
final class InputState(
    val mouse: Mouse,
    val keyboard: Keyboard,
    val gamepad: Gamepad,
    val wheel: Wheel,
    val pen: Pen,
    val touch: Touch,
    val pointers: Pointers
) {

  /** Given some input mappings, produce a guaranteed value A based on the current InputState.
    */
  def mapInputs[A](mappings: InputMapping[A], default: A): A =
    mappings.find(mouse, wheel, keyboard, gamepad).getOrElse(default)

  /** Given some input mappings, produce an optional value A based on the current InputState.
    */
  def mapInputsOption[A](mappings: InputMapping[A]): Option[A] =
    mappings.find(mouse, wheel, keyboard, gamepad)

}

object InputState {
  val default: InputState =
    InputState(
      Mouse.default,
      Keyboard.default,
      Gamepad.default,
      Wheel.default,
      Pen.default,
      Touch.default,
      Pointers.default
    )

  def calculateNext(
      previous: InputState,
      events: Batch[InputEvent],
      gamepadState: Gamepad
  ): InputState =
    val pointers = Pointers.calculateNext(previous.pointers, events.collect { case e: PointerEvent => e });

    @nowarn("msg=deprecated")
    val state = InputState(
      Mouse(pointers, events.collect { case e: MouseEvent.Wheel => e }),
      Keyboard.calculateNext(previous.keyboard, events.collect { case e: KeyboardEvent => e }),
      gamepadState,
      Wheel(events.collect { case e: WheelEvent.Move => e }),
      Pen(pointers),
      Touch(pointers),
      pointers
    )

    state
}
