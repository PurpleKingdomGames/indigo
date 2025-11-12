package indigo.shared.events

import indigo.shared.constants.Key
import indigo.shared.datatypes.Point
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.MouseState
import indigo.shared.input.Wheel

/** Input mapping instances describe combinations of "live" inputs like key combinations or gamepad buttons, and map
  * them to some user defined value.
  */
final case class InputMapping[A](oneOf: List[(Combo, A)]) {

  def add(combos: (Combo, A)*): InputMapping[A] =
    add(combos.toList)
  def add(combos: List[(Combo, A)]): InputMapping[A] =
    this.copy(oneOf = oneOf ++ combos)

  def find(mouse: MouseState, wheel: Wheel, keyboard: Keyboard, gamepad: Gamepad): Option[A] =
    oneOf
      .find { c =>
        c._1.mouseInputs.forall {
          case MouseInput.MouseUp                 => mouse.isReleased
          case MouseInput.MouseDown               => mouse.isPressed
          case MouseInput.MouseClick              => mouse.isClicked
          case MouseInput.MouseAt(pt)             => mouse.position == pt
          case MouseInput.MouseButtonUp(button)   => mouse.released(button)
          case MouseInput.MouseButtonDown(button) => mouse.pressed(button)
          case MouseInput.MouseWheelDown          => wheel.verticalScroll.contains(WheelDirection.Down)
          case MouseInput.MouseWheelUp            => wheel.verticalScroll.contains(WheelDirection.Up)
        } &&
        c._1.keyInputs.forall(k => keyboard.keysDown.contains(k)) &&
        c._1.gamepadInputs.forall {
          case GamepadInput.DPAD_UP    => gamepad.dpad.up
          case GamepadInput.DPAD_LEFT  => gamepad.dpad.left
          case GamepadInput.DPAD_RIGHT => gamepad.dpad.right
          case GamepadInput.DPAD_DOWN  => gamepad.dpad.down
          case GamepadInput.Cross      => gamepad.buttons.Cross
          case GamepadInput.Circle     => gamepad.buttons.Circle
          case GamepadInput.Square     => gamepad.buttons.Square
          case GamepadInput.Triangle   => gamepad.buttons.Triangle
          case GamepadInput.L1         => gamepad.buttons.L1
          case GamepadInput.L2         => gamepad.buttons.L2
          case GamepadInput.R1         => gamepad.buttons.R1
          case GamepadInput.R2         => gamepad.buttons.R2
          case GamepadInput.Options    => gamepad.buttons.Options
          case GamepadInput.Share      => gamepad.buttons.Share
          case GamepadInput.PS         => gamepad.buttons.PS
          case GamepadInput.TouchPad   => gamepad.buttons.TouchPad

          case GamepadInput.LEFT_ANALOG(xp, yp, pressed) =>
            xp(gamepad.analog.left.x) && yp(gamepad.analog.left.y) && gamepad.analog.left.pressed == pressed

          case GamepadInput.RIGHT_ANALOG(xp, yp, pressed) =>
            xp(gamepad.analog.right.x) && yp(gamepad.analog.right.y) && gamepad.analog.right.pressed == pressed
        }
      }
      .map(_._2)

}
object InputMapping {

  def apply[A](): InputMapping[A] =
    empty

  def empty[A]: InputMapping[A] =
    InputMapping(Nil)

  def apply[A](combos: (Combo, A)*): InputMapping[A] =
    InputMapping(combos.toList)

}

final case class Combo(mouseInputs: List[MouseInput], keyInputs: List[Key], gamepadInputs: List[GamepadInput]) {
  def |+|(other: Combo): Combo =
    Combo(mouseInputs ++ other.mouseInputs, keyInputs ++ other.keyInputs, gamepadInputs ++ other.gamepadInputs)

  def withMouseInputs(newInputs: MouseInput*): Combo =
    withMouseInputs(newInputs.toList)
  def withMouseInputs(newInputs: List[MouseInput]): Combo =
    this.copy(mouseInputs = mouseInputs ++ newInputs)

  def withKeyInputs(newInputs: Key*): Combo =
    withKeyInputs(newInputs.toList)
  def withKeyInputs(newInputs: List[Key]): Combo =
    this.copy(keyInputs = keyInputs ++ newInputs)

  def withGamepadInputs(newInputs: GamepadInput*): Combo =
    withGamepadInputs(newInputs.toList)
  def withGamepadInputs(newInputs: List[GamepadInput]): Combo =
    this.copy(gamepadInputs = gamepadInputs ++ newInputs)

}
object Combo {

  def empty: Combo =
    Combo(Nil, Nil, Nil)

  def MouseInputs(inputs: MouseInput*): Combo =
    MouseInputs(inputs.toList)
  def MouseInputs(inputs: List[MouseInput]): Combo =
    Combo(inputs, Nil, Nil)

  def KeyInputs(inputs: Key*): Combo =
    KeyInputs(inputs.toList)
  def KeyInputs(inputs: List[Key]): Combo =
    Combo(Nil, inputs, Nil)

  def GamepadInputs(inputs: GamepadInput*): Combo =
    GamepadInputs(inputs.toList)
  def GamepadInputs(inputs: List[GamepadInput]): Combo =
    Combo(Nil, Nil, inputs)

  def withMouseInputs(newInputs: MouseInput*): Combo =
    withMouseInputs(newInputs.toList)
  def withMouseInputs(newInputs: List[MouseInput]): Combo =
    MouseInputs(newInputs)

  def withKeyInputs(newInputs: Key*): Combo =
    withKeyInputs(newInputs.toList)
  def withKeyInputs(newInputs: List[Key]): Combo =
    KeyInputs(newInputs)

  def withGamepadInputs(newInputs: GamepadInput*): Combo =
    withGamepadInputs(newInputs.toList)
  def withGamepadInputs(newInputs: List[GamepadInput]): Combo =
    GamepadInputs(newInputs)
}

enum GamepadInput derives CanEqual:
  case DPAD_UP    extends GamepadInput
  case DPAD_LEFT  extends GamepadInput
  case DPAD_RIGHT extends GamepadInput
  case DPAD_DOWN  extends GamepadInput
  case Cross      extends GamepadInput
  case Circle     extends GamepadInput
  case Square     extends GamepadInput
  case Triangle   extends GamepadInput
  case L1         extends GamepadInput
  case L2         extends GamepadInput
  case R1         extends GamepadInput
  case R2         extends GamepadInput
  case Options    extends GamepadInput
  case Share      extends GamepadInput
  case PS         extends GamepadInput
  case TouchPad   extends GamepadInput
  case LEFT_ANALOG(
      x: Double => Boolean,
      y: Double => Boolean,
      pressed: Boolean
  ) extends GamepadInput
  case RIGHT_ANALOG(
      x: Double => Boolean,
      y: Double => Boolean,
      pressed: Boolean
  ) extends GamepadInput

enum MouseInput derives CanEqual:
  case MouseDown                            extends MouseInput
  case MouseUp                              extends MouseInput
  case MouseClick                           extends MouseInput
  case MouseAt(position: Point)             extends MouseInput
  case MouseButtonDown(button: MouseButton) extends MouseInput
  case MouseButtonUp(button: MouseButton)   extends MouseInput
  case MouseWheelUp                         extends MouseInput
  case MouseWheelDown                       extends MouseInput

object MouseInput:
  object MouseAt:
    def apply(x: Int, y: Int): MouseAt =
      MouseAt(Point(x, y))
