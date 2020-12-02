package indigo.platform.input

import org.scalajs.dom.window
import org.scalajs.dom.experimental.gamepad.GamepadNavigator
import org.scalajs.dom.experimental.gamepad.{Gamepad => GamepadJS}
import indigo.shared.input.{Gamepad, GamepadButtons, GamepadDPad, AnalogAxis, GamepadInputCapture}
import indigo.shared.input.GamepadAnalogControls

object GamepadInputCaptureImpl {

  def apply(): GamepadInputCapture =
    new GamepadInputCapture {
      def giveGamepadState: Gamepad =
        GamepadInputCaptureImpl.giveGamepadState
    }

  /* PS4 Layout
  Axis array:
  0 left stick X (double 1 is right -1 is left)
  1 left stick Y (double 1 is down -1 is up)
  2 right stick X (double 1 is right -1 is left)
  3 right stick Y (double 1 is down -1 is up)

  Buttons array:
  0 X
  1 O
  2 Square
  3 Triangle
  4 L1
  5 R1
  6 L2
  7 R2
  8 Share
  9 Options
  10 Left Stick Press
  11 Right Stick Press
  12 D Up
  13 D Down
  14 D Left
  15 D Right
  16 PS Button
  17 Touch pad press
   */

  def giveGamepadState: Gamepad =
    gamepads.filter(_.connected).headOption match {
      case Some(gp) =>
        new Gamepad(
          connected = true,
          new GamepadAnalogControls(
            new AnalogAxis(gp.axes(0), gp.axes(1), gp.buttons(10).pressed),
            new AnalogAxis(gp.axes(2), gp.axes(3), gp.buttons(11).pressed)
          ),
          new GamepadDPad(
            gp.buttons(12).pressed,
            gp.buttons(13).pressed,
            gp.buttons(14).pressed,
            gp.buttons(15).pressed
          ),
          new GamepadButtons(
            gp.buttons(0).pressed,
            gp.buttons(1).pressed,
            gp.buttons(2).pressed,
            gp.buttons(3).pressed,
            gp.buttons(4).pressed,
            gp.buttons(5).pressed,
            gp.buttons(6).pressed,
            gp.buttons(7).pressed,
            gp.buttons(8).pressed,
            gp.buttons(9).pressed,
            gp.buttons(16).pressed,
            gp.buttons(17).pressed
          )
        )

      case None =>
        Gamepad.default
    }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var gamepads: scalajs.js.Array[GamepadJS] = new scalajs.js.Array()

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def init(): Unit = {
    window.addEventListener(
      "gamepadconnected", { (_: Any) =>
        gamepads = window.navigator.asInstanceOf[GamepadNavigator].getGamepads()
      },
      false
    )

    window.addEventListener(
      "gamepaddisconnected", { (_: Any) =>
        gamepads = window.navigator.asInstanceOf[GamepadNavigator].getGamepads()
      },
      false
    )
  }

}
