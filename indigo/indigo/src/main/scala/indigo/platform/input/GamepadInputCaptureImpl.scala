package indigo.platform.input

import indigo.shared.input.AnalogAxis
import indigo.shared.input.Gamepad
import indigo.shared.input.GamepadAnalogControls
import indigo.shared.input.GamepadButtons
import indigo.shared.input.GamepadDPad
import indigo.shared.input.GamepadInputCapture
import org.scalajs.dom.Gamepad as GamepadJS
import org.scalajs.dom.window

object GamepadInputCaptureImpl {

  private given CanEqual[Option[GamepadJS], Option[GamepadJS]] = CanEqual.derived

  def apply(): GamepadInputCapture =
    new GamepadInputCapture {
      def giveGamepadState: Gamepad =
        GamepadInputCaptureImpl.giveGamepadState
    }

  // TODO definitely a ugly workaround, but this is the most direct one until we have a more sound design
  // on controller layouts and how to represent them in code, currently even `GamepadButtons` is hardcoded
  // to PS-specific buttons
  private val ps4ControllerVendorProduct = Seq("054c", "05c4")

  private def usesTouchpad(id: String): Boolean =
    ps4ControllerVendorProduct.forall(id.contains(_))

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
    // TODO some browsers like Firefox do this internally (apparently, couldn't find more information about it)
    // Others based on Webkit do not, hence we need to update the gamepads state everytime we reach this point of
    // the game loop
    gamepads = window.navigator.getGamepads()

    // Filter won't work here since some browsers like Chromium return `null` values in the gamepads array
    gamepads.find(Option(_).exists(_.connected)) match {
      case Some(gp) =>
        val gameAnalogControls = {
          val numberOfAxes = gp.axes.length / 2
          GamepadAnalogControls(
            AnalogAxis(gp.axes(0), gp.axes(1), gp.buttons(10).pressed),
            if numberOfAxes >= 2 then AnalogAxis(gp.axes(2), gp.axes(3), gp.buttons(11).pressed)
            else AnalogAxis.default,
            numberOfAxes
          )
        }
        new Gamepad(
          connected = true,
          gameAnalogControls,
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
            usesTouchpad(gp.id) && gp.buttons(17).pressed
          )
        )

      case None =>
        Gamepad.default
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var gamepads: scalajs.js.Array[GamepadJS] = new scalajs.js.Array()

  private val handler =
    (_: Any) => gamepads = window.navigator.getGamepads()

  def init(): Unit = {
    window.addEventListener("gamepadconnected", handler, false)
    window.addEventListener("gamepaddisconnected", handler, false)
  }

  def kill(): Unit = {
    window.removeEventListener("gamepadconnected", handler, false)
    window.removeEventListener("gamepaddisconnected", handler, false)
  }

}
