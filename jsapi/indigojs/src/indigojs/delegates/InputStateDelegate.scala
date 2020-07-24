package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.events.InputState
import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.constants.Key
import indigo.shared.input.GamepadDPad
import indigo.shared.input.GamepadAnalogControls
import indigo.shared.input.GamepadButtons
import indigo.shared.input.AnalogAxis

@SuppressWarnings(Array("org.wartremover.warts.Any"))
final class InputStateDelegate(inputState: InputState) {

  @JSExport
  val mouse: MouseDelegate =
    new MouseDelegate(inputState.mouse)

  @JSExport
  val keyboard: KeyboardDelegate =
    new KeyboardDelegate(inputState.keyboard)

  @JSExport
  val gamepad: GamepadDelegate =
    new GamepadDelegate(inputState.gamepad)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Mouse")
@JSExportAll
final class MouseDelegate(mouse: Mouse) {

  val position: PointDelegate                  = new PointDelegate(mouse.position.x, mouse.position.y)
  val leftMouseIsDown: Boolean                 = mouse.leftMouseIsDown
  lazy val mousePressed: Boolean               = mouse.mousePressed
  lazy val mouseReleased: Boolean              = mouse.mouseReleased
  lazy val mouseClicked: Boolean               = mouse.mouseClicked
  lazy val mouseClickAt: Option[PointDelegate] = mouse.mouseClickAt.map(p => new PointDelegate(p.x, p.y))
  lazy val mouseUpAt: Option[PointDelegate]    = mouse.mouseUpAt.map(p => new PointDelegate(p.x, p.y))
  lazy val mouseDownAt: Option[PointDelegate]  = mouse.mouseDownAt.map(p => new PointDelegate(p.x, p.y))

  def wasMouseClickedAt(x: Int, y: Int): Boolean                               = mouse.wasMouseClickedAt(x, y)
  def wasMouseUpAt(x: Int, y: Int): Boolean                                    = mouse.wasMouseUpAt(x, y)
  def wasMouseDownAt(x: Int, y: Int): Boolean                                  = mouse.wasMouseDownAt(x, y)
  def wasMousePositionAt(x: Int, y: Int): Boolean                              = mouse.wasMousePositionAt(x, y)
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean  = mouse.wasMouseClickedWithin(x, y, width, height)
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean       = mouse.wasMouseUpWithin(x, y, width, height)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean     = mouse.wasMouseDownWithin(x, y, width, height)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean = mouse.wasMousePositionWithin(x, y, width, height)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Keyboard")
@JSExportAll
final class KeyboardDelegate(keyboard: Keyboard) {
  val keysDown: List[Key]                   = keyboard.keysDown
  val lastKeyHeldDown: Option[Key]          = keyboard.lastKeyHeldDown
  lazy val keysReleased: List[Key]          = keyboard.keysReleased
  def keysAreDown(keys: List[Key]): Boolean = keyboard.keysAreDown(keys: _*)
  def keysAreUp(keys: List[Key]): Boolean   = keyboard.keysAreUp(keys: _*)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GamepadState")
@JSExportAll
final class GamepadDelegate(gamepadState: Gamepad) {
  val connected: Boolean                    = gamepadState.connected
  val analog: GamepadAnalogControlsDelegate = new GamepadAnalogControlsDelegate(gamepadState.analog)
  val dpad: GamepadDPadDelegate             = new GamepadDPadDelegate(gamepadState.dpad)
  val buttons: GamepadButtonsDelegate       = new GamepadButtonsDelegate(gamepadState.buttons)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GamepadAnalogControls")
@JSExportAll
final class GamepadAnalogControlsDelegate(value: GamepadAnalogControls) {
  val left: AnalogAxisDelegate  = new AnalogAxisDelegate(value.left)
  val right: AnalogAxisDelegate = new AnalogAxisDelegate(value.right)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GamepadDPad")
@JSExportAll
final class GamepadDPadDelegate(value: GamepadDPad) {
  val up: Boolean    = value.up
  val down: Boolean  = value.down
  val left: Boolean  = value.left
  val right: Boolean = value.right
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("GamepadButtons")
@JSExportAll
final class GamepadButtonsDelegate(value: GamepadButtons) {
  val Cross: Boolean    = value.Cross
  val Circle: Boolean   = value.Circle
  val Square: Boolean   = value.Square
  val Triangle: Boolean = value.Triangle
  val L1: Boolean       = value.L1
  val L2: Boolean       = value.L2
  val R1: Boolean       = value.R1
  val R2: Boolean       = value.R2
  val Options: Boolean  = value.Options
  val Share: Boolean    = value.Share
  val PS: Boolean       = value.PS
  val TouchPad: Boolean = value.TouchPad
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AnalogAxis")
@JSExportAll
final class AnalogAxisDelegate(value: AnalogAxis) {
  val x: Double        = value.x
  val y: Double        = value.y
  val pressed: Boolean = value.pressed
}
