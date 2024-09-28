package indigo.shared.input

final case class Gamepad(connected: Boolean, analog: GamepadAnalogControls, dpad: GamepadDPad, buttons: GamepadButtons)
    derives CanEqual

object Gamepad {

  val default: Gamepad =
    Gamepad(
      connected = false,
      GamepadAnalogControls.default,
      GamepadDPad.default,
      GamepadButtons.default
    )

  def unapply(value: Gamepad): Option[(Boolean, GamepadAnalogControls, GamepadDPad, GamepadButtons)] =
    Some(
      (
        value.connected,
        value.analog,
        value.dpad,
        value.buttons
      )
    )

}

final class GamepadButtons(
    val Cross: Boolean,
    val Circle: Boolean,
    val Square: Boolean,
    val Triangle: Boolean,
    val L1: Boolean,
    val L2: Boolean,
    val R1: Boolean,
    val R2: Boolean,
    val Options: Boolean,
    val Share: Boolean,
    val PS: Boolean,
    val TouchPad: Boolean
)
object GamepadButtons {
  val default: GamepadButtons =
    new GamepadButtons(
      Cross = false,
      Circle = false,
      Square = false,
      Triangle = false,
      L1 = false,
      R1 = false,
      L2 = false,
      R2 = false,
      Share = false,
      Options = false,
      PS = false,
      TouchPad = false
    )

  def unapply(value: GamepadButtons): Option[
    (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean)
  ] =
    Some(
      (
        value.Cross,
        value.Circle,
        value.Square,
        value.Triangle,
        value.L1,
        value.R1,
        value.L2,
        value.R2,
        value.Share,
        value.Options,
        value.PS,
        value.TouchPad
      )
    )
}

final case class GamepadDPad(up: Boolean, down: Boolean, left: Boolean, right: Boolean)
object GamepadDPad {
  val default: GamepadDPad =
    GamepadDPad(false, false, false, false)
}

final case class GamepadAnalogControls(left: AnalogAxis, right: AnalogAxis, numberOfAxes: Int)
object GamepadAnalogControls {
  val default: GamepadAnalogControls =
    GamepadAnalogControls(AnalogAxis.default, AnalogAxis.default, 0)
}

final case class AnalogAxis(x: Double, y: Double, pressed: Boolean)
object AnalogAxis {
  val default: AnalogAxis =
    AnalogAxis(0d, 0d, false)
}
