package indigo.shared.subsystems

import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.Gamepad
import indigo.shared.input.Keyboard
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneNode
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

/** Similar to [FrameContext] but without access to start up data. The SubSystemFrameContext is the context in which the
  * current frame will be processed. In includes values that are unique to this frame, and also globally available
  * services.
  *
  * @param gameTime
  *   A sampled instance of time that you should use everywhere that you need a time value.
  * @param dice
  *   A psuedorandom number generator, made predicatable/reproducable by being seeded on the current running time.
  * @param inputState
  *   A snapshot of the state of the various input methods, also allows input mapping of combinations of inputs.
  * @param boundaryLocator
  *   A service that can be interogated for the calculated dimensions of screen elements.
  */
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

  def findBounds(sceneGraphNode: SceneNode): Option[Rectangle] =
    boundaryLocator.findBounds(sceneGraphNode)

  def bounds(sceneGraphNode: SceneNode): Rectangle =
    boundaryLocator.bounds(sceneGraphNode)

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
