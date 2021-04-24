package indigo.shared.input

final class Gamepad(val connected: Boolean, val analog: GamepadAnalogControls, val dpad: GamepadDPad, val buttons: GamepadButtons)

object Gamepad {

  val default: Gamepad =
    new Gamepad(
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

  def unapply(value: GamepadButtons): Option[(Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean)] =
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

final class GamepadDPad(val up: Boolean, val down: Boolean, val left: Boolean, val right: Boolean)
object GamepadDPad {
  val default: GamepadDPad =
    new GamepadDPad(false, false, false, false)

  def unapply(value: GamepadDPad): Option[(Boolean, Boolean, Boolean, Boolean)] =
    Some((value.up, value.down, value.left, value.right))
}

final class GamepadAnalogControls(val left: AnalogAxis, val right: AnalogAxis)
object GamepadAnalogControls {
  val default: GamepadAnalogControls =
    new GamepadAnalogControls(AnalogAxis.default, AnalogAxis.default)

  def unapply(value: GamepadAnalogControls): Option[(AnalogAxis, AnalogAxis)] =
    Some((value.left, value.right))
}

final class AnalogAxis(val x: Double, val y: Double, val pressed: Boolean)
object AnalogAxis {
  val default: AnalogAxis =
    new AnalogAxis(0d, 0d, false)

  def unapply(value: AnalogAxis): Option[(Double, Double, Boolean)] =
    Some((value.x, value.y, value.pressed))
}
