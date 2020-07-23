package indigo.shared.subsystems

import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.datatypes.Rectangle
import indigo.shared.input.Mouse
import indigo.shared.input.Keyboard
import indigo.shared.input.Gamepad
import indigo.shared.time.Seconds
import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext

final class SubSystemFrameContext(
    val gameTime: GameTime,
    val dice: Dice,
    val inputState: InputState,
    val boundaryLocator: BoundaryLocator
) {

  val running: Seconds = gameTime.running
  val delta: Seconds   = gameTime.delta

  val mouse: Mouse       = inputState.mouse
  val keyboard: Keyboard = inputState.keyboard
  val gamepad: Gamepad   = inputState.gamepad

  def findBounds(sceneGraphNode: SceneGraphNode): Rectangle =
    boundaryLocator.findBounds(sceneGraphNode)

}
object SubSystemFrameContext {

  implicit class FrameContextForSubSystems(frameContext: FrameContext[_]) {
    def forSubSystems: SubSystemFrameContext =
      new SubSystemFrameContext(
        frameContext.gameTime,
        frameContext.dice,
        frameContext.inputState,
        frameContext.boundaryLocator
      )
  }

}
