package indigo.platform.input

import indigo.shared.input.Gamepad
import indigo.shared.input.GamepadInputCapture

object GamepadInputCaptureImpl {

  def apply(): GamepadInputCapture =
    new GamepadInputCapture {
      def giveGamepadState: Gamepad =
        GamepadInputCaptureImpl.giveGamepadState
    }

  def giveGamepadState: Gamepad =
    Gamepad.default

}
