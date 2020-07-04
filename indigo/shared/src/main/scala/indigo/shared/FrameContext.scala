package indigo.shared

import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.Mouse
import indigo.shared.events.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.time.Seconds

final class FrameContext[StartUpData](
    val gameTime: GameTime,
    val dice: Dice,
    val inputState: InputState,
    val boundaryLocator: BoundaryLocator,
    _startUpData: => StartUpData
) {

  lazy val startUpData = _startUpData
  val running: Seconds = gameTime.running
  val delta: Seconds   = gameTime.delta

  val mouse: Mouse       = inputState.mouse
  val keyboard: Keyboard = inputState.keyboard
  val gamepad: Gamepad   = inputState.gamepad

  def findBounds(sceneGraphNode: SceneGraphNode): Rectangle =
    boundaryLocator.findBounds(sceneGraphNode)

}
